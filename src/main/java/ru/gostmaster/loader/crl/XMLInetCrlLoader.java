package ru.gostmaster.loader.crl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import ru.gostmaster.common.reactor.FluxHelper;
import ru.gostmaster.common.data.crl.Crl;
import ru.gostmaster.common.spi.loader.CRLLoader;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

@Slf4j
@Component
public class XMLInetCrlLoader implements CRLLoader {

    private String url;
    private FluxHelper fluxHelper;

    @Override
    public Flux<Crl> loadCertificateRevocationLists() {
        Flux<String> urlFlux = Flux.create(crlFluxSink -> {
            try {
                log.info("Preparing to extract cert list from website {}", url);
                URLConnection urlConnection = new URL(url).openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                XMLCRLDefaultHandler handler = new XMLCRLDefaultHandler(crlFluxSink);
                parser.parse(inputStream, handler);
                log.info("All crl addresses are extracted from {}", url);
            } catch (Exception ex) {
                crlFluxSink.error(ex);
            }
        });

        Flux<Crl> res = fluxHelper.getCrlFluxFromUrls(urlFlux);
        return res;
    }

    @Value("${cert.xml.data.url}")
    public void setUrl(String url) {
        this.url = url;
    }

    @Autowired
    public void setFluxHelper(FluxHelper fluxHelper) {
        this.fluxHelper = fluxHelper;
    }
}

@Slf4j
class XMLCRLDefaultHandler extends DefaultHandler {
    private FluxSink<String> fluxSink;
    private StringBuilder urlBuilder = new StringBuilder();

    private static final String CRL_PARENT_ELEMENT = "АдресаСписковОтзыва";
    private static final String CRL_URL = "Адрес";
    private String currentElement;
    private String previousElement;
    private Boolean inCrlAddress = false;

    public XMLCRLDefaultHandler(FluxSink<String> fluxSink) {
        this.fluxSink = fluxSink;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        previousElement = currentElement;
        currentElement = qName;
        inCrlAddress = CRL_PARENT_ELEMENT.equals(previousElement) && CRL_URL.equals(currentElement);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (inCrlAddress) {
            String address = urlBuilder.toString();
            log.debug("Added address {}", address);
            fluxSink.next(address);
            urlBuilder = new StringBuilder();
            inCrlAddress = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inCrlAddress) {
            urlBuilder.append(ch, start, length);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        fluxSink.complete();
    }
}