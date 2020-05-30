package ru.gostmaster.config;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.gostmaster.common.core.updater.CRLUpdater;
import ru.gostmaster.common.core.updater.CertificateUpdater;
import ru.gostmaster.common.spi.loader.CRLLoader;
import ru.gostmaster.loader.cert.DirectoryCertificateLoader;
import ru.gostmaster.loader.cert.XMLInetCertificateLoader;
import ru.gostmaster.loader.crl.ListCrlLoader;
import ru.gostmaster.loader.crl.XMLInetCrlLoader;
import ru.gostmaster.storage.MongoCertificateStorage;
import ru.gostmaster.storage.MongoCrlStorage;

import java.util.Arrays;
import java.util.List;

/**
 * Класс для Spring Java кофига приложения.
 * 
 * @author maksimgurin 
 */
@Configuration
public class Config {

    /**
     * Bean для загрузки сертификатов.
     * @param inetLoader загрузчик сертификатов из интернета с сайта Минкомсвязи
     * @param dirLoader загрузчик сертификатов из директории
     * @param storage ссылка на хранилище сертификатов.
     * @return bean
     */
    @Autowired
    @Bean
    public CertificateUpdater certificateUpdater(XMLInetCertificateLoader inetLoader,
                                                 DirectoryCertificateLoader dirLoader,
                                                 MongoCertificateStorage storage) {
        return new CertificateUpdater(Lists.newArrayList(dirLoader), Lists.newArrayList(inetLoader), storage);
    }

    /**
     * Bean для загрузки списка отозванных сертификатов.
     * @param inetLoader загрузчик CRL из интернета с сайта Минкомсвязи
     * @param listCrlLoader загрузчик CRL из списка в  файле
     * @param storage ссылка на хранилище CRL.
     * @return bean
     */
    @Autowired
    @Bean
    public CRLUpdater crlUpdater(XMLInetCrlLoader inetLoader, 
                                 ListCrlLoader listCrlLoader, 
                                 MongoCrlStorage storage) {
        List<CRLLoader> loaders = Arrays.asList(listCrlLoader, inetLoader);
        CRLUpdater updater = new CRLUpdater(loaders, storage);
        return updater;
    }

    /**
     * RestTemplate.
     * @return restTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
