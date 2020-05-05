package ru.gostmaster.loader.cert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import ru.gostmaster.common.data.cert.Certificate;
import ru.gostmaster.common.spi.loader.CertificateLoader;
import ru.gostmaster.model.MongoCertificateData;
import ru.gostmaster.parser.CertificateParser;
import sun.security.provider.X509Factory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

@Component
@Slf4j
public class XMLInetCertificateLoader implements CertificateLoader {
    
    private String url;
    private CertificateParser certificateParser;
    private boolean trusted = false;

    @Override
    public Flux<Certificate> loadCertificates() {
        
        Flux<Certificate> certificateFlux = Flux.<Certificate>create(fluxSink -> {
            try {
                log.info("Preparing to extract cert list from website {}", url);
                URLConnection urlConnection = new URL(url).openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                XMLCertificateDefaultHandler handler = new XMLCertificateDefaultHandler(fluxSink, trusted, certificateParser);
                parser.parse(inputStream, handler);
                log.warn("Cert list extracted!");
            } catch (Exception ex) {
                log.error("", ex);
                fluxSink.error(ex);
            }
        });
        
        return certificateFlux;
    }

    @Value("${cert.xml.data.url}")
    public void setUrl(String url) {
        this.url = url;
    }

    @Autowired
    public void setCertificateParser(CertificateParser certificateParser) {
        this.certificateParser = certificateParser;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }
}

@Slf4j
class XMLCertificateDefaultHandler extends DefaultHandler {
    private FluxSink<Certificate> fluxSink;
    private boolean trusted;
    private CertificateParser certificateParser;
    private StringBuilder certBuilder = new StringBuilder();
    
    private final String CERT_DATA_ENCODED_START_NAME = "Данные";
    
    private boolean readingCertData = false;

    public XMLCertificateDefaultHandler(FluxSink<Certificate> fluxSink, boolean trusted, CertificateParser certificateParser) {
        this.fluxSink = fluxSink;
        this.trusted = trusted;
        this.certificateParser = certificateParser;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (CERT_DATA_ENCODED_START_NAME.equals(qName)) {
            readingCertData = true;
            certBuilder = new StringBuilder(X509Factory.BEGIN_CERT).append("\n");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (CERT_DATA_ENCODED_START_NAME.equals(qName)) {
            readingCertData = false;
            certBuilder.append("\n").append(X509Factory.END_CERT);
            String certData = certBuilder.toString();
            try {
                MongoCertificateData data = certificateParser.parseRawDataCertificate(certData);
                data.setTrusted(trusted);
                log.debug("Extracted cert {} for subject {}", data.getSn(), data.getSubject());
                fluxSink.next(data);
            } catch (Exception ex) {
                log.error("", ex);
                fluxSink.error(ex);
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        fluxSink.complete();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (readingCertData) {
            certBuilder.append(ch, start, length);
        }
    }
}
