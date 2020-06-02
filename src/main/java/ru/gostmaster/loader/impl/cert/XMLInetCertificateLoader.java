package ru.gostmaster.loader.impl.cert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import ru.gostmaster.data.cert.Certificate;
import ru.gostmaster.loader.CertificateLoader;
import ru.gostmaster.parser.CertificateParser;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Компонент-загрузчик сертификатов с сайта Минкомсвязи.
 * 
 * @author maksimgurin 
 */
@Slf4j
@Component
public class XMLInetCertificateLoader implements CertificateLoader {

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";
    
    private String url;
    private CertificateParser certificateParser;

    @Override
    public Flux<Certificate> loadCertificates() {
        
        Flux<Certificate> certificateFlux = Flux.<Certificate>create(fluxSink -> {
            try {
                log.info("Preparing to extract cert list from website {}", url);
                URLConnection urlConnection = new URL(url).openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                XMLCertificateDefaultHandler handler = new XMLCertificateDefaultHandler(fluxSink, certificateParser);
                parser.parse(inputStream, handler);
                log.warn("Cert list extracted!");
            } catch (Exception ex) {
                log.error("", ex);
                fluxSink.error(ex);
            }
        }).cache();
        
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

    /**
     * SAX handler, который отдает загруженные сертификаты по мере из получения.
     * 
     * @author maksimgurin 
     */
    @Slf4j
    private static class XMLCertificateDefaultHandler extends DefaultHandler {
        private FluxSink<Certificate> fluxSink;
        private CertificateParser certificateParser;
        private StringBuilder certBuilder = new StringBuilder();

        private final String certPemDataElementName = "Данные";

        private boolean readingPem;

        XMLCertificateDefaultHandler(FluxSink<Certificate> fluxSink, CertificateParser certificateParser) {
            this.fluxSink = fluxSink;
            this.certificateParser = certificateParser;
        }

        @Override
        public void startDocument() throws SAXException {
            log.info("Start SAX parsing...");
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (certPemDataElementName.equals(qName)) {
                readingPem = true;
                certBuilder = new StringBuilder(BEGIN_CERT).append("\n");
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (certPemDataElementName.equals(qName)) {
                readingPem = false;
                certBuilder.append("\n").append(END_CERT);
                String certData = certBuilder.toString();
                try {
                    Certificate data = certificateParser.parseRawDataCertificate(certData);
                    log.debug("Extracted cert {} with subject key {}", data.getSn(), data.getSubjectKey());
                    fluxSink.next(data);
                } catch (Exception ex) {
                    log.error("", ex);
                    fluxSink.error(ex);
                }
            }
        }

        @Override
        public void endDocument() throws SAXException {
            log.info("End SAX parsing");
            fluxSink.complete();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (readingPem) {
                certBuilder.append(ch, start, length);
            }
        }
    }
}
