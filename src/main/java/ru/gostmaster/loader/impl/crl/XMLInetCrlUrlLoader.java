package ru.gostmaster.loader.impl.crl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import ru.gostmaster.loader.CRLUrlLoader;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Компонент-загрузчик для загрузки CRL с сайта Минкомсвязи.
 *
 * @author maksimgurin
 */
@Slf4j
@Component
public class XMLInetCrlUrlLoader implements CRLUrlLoader {

    private String url;

    @Override
    public Flux<String> loadCrlUrls() {
        Flux<String> urlFlux = Flux.create(crlFluxSink -> {
            try {
                log.info("Preparing to extract crl list from website {}", url);
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
        Flux<String> cachedUrlFlux = urlFlux.cache();
        return cachedUrlFlux;
    }

    @Value("${cert.xml.data.url}")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * SAX hadnler, который отдает ссылки на CRL по мере их получения из XML&.
     *
     * @author maksimgurin
     */
    @Slf4j
    private static class XMLCRLDefaultHandler extends DefaultHandler {
        private static final String CRL_PARENT_ELEMENT = "АдресаСписковОтзыва";
        private static final String CRL_URL = "Адрес";
        
        private FluxSink<String> fluxSink;
        private StringBuilder urlBuilder = new StringBuilder();
        private String currentElement;
        private String previousElement;
        private Boolean inCrlAddress = false;

        XMLCRLDefaultHandler(FluxSink<String> fluxSink) {
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
}