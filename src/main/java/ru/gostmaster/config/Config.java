package ru.gostmaster.config;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.gostmaster.loader.CRLUrlLoader;
import ru.gostmaster.loader.impl.cert.DirectoryCertificateLoader;
import ru.gostmaster.loader.impl.cert.XMLInetCertificateLoader;
import ru.gostmaster.loader.impl.crl.ListCrlUrlLoader;
import ru.gostmaster.loader.impl.crl.XMLInetCrlUrlLoader;
import ru.gostmaster.storage.impl.MongoCertificateStorage;
import ru.gostmaster.storage.impl.MongoCrlUrlStorage;
import ru.gostmaster.updater.CRLUrlUpdater;
import ru.gostmaster.updater.CertificateUpdater;
import ru.gostmaster.verification.Check;
import ru.gostmaster.verification.VerificationChecksService;
import ru.gostmaster.verification.impl.VerificationChecksServiceIImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Класс для Spring Java кофига приложения.
 * 
 * @author maksimgurin 
 */
@Configuration
public class Config {

    /**
     * Bean для загрузки сертификатов.
     *
     * @param inetLoader    загрузчик сертификатов из интернета с сайта Минкомсвязи
     * @param dirLoader     загрузчик сертификатов из директории
     * @param storage       ссылка на хранилище сертификатов.
     * @param crlUrlStorage хранилище ссылок CRL.
     * @return bean
     */
    @Autowired
    @Bean
    public CertificateUpdater certificateUpdater(XMLInetCertificateLoader inetLoader,
                                                 DirectoryCertificateLoader dirLoader,
                                                 MongoCertificateStorage storage,
                                                 MongoCrlUrlStorage crlUrlStorage) {
        return new CertificateUpdater(Lists.newArrayList(dirLoader), Lists.newArrayList(inetLoader),
            storage, crlUrlStorage);
    }

    /**
     * Bean для загрузки списка отозванных сертификатов.
     *
     * @param inetLoader    загрузчик ссылок на CRL из интернета с сайта Минкомсвязи
     * @param listCrlLoader загрузчик ссылок на CRL из списка в файле
     * @param storage       ссылка на хранилище ссыдлк CRL.
     * @return bean
     */
    @Autowired
    @Bean
    public CRLUrlUpdater crlUpdater(XMLInetCrlUrlLoader inetLoader,
                                    ListCrlUrlLoader listCrlLoader,
                                    MongoCrlUrlStorage storage) {
        List<CRLUrlLoader> loaders = Arrays.asList(listCrlLoader, inetLoader);
        CRLUrlUpdater updater = new CRLUrlUpdater(loaders, storage);
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

    /**
     * Автоматом подтягиваем из контекста все проверки.
     * @param applicationContext контекст
     * @return объект со списком проверок
     */
    @Bean
    @Autowired
    public VerificationChecksService verificationChecksService(ApplicationContext applicationContext) {
        Map<String, Check> beansOfType = applicationContext.getBeansOfType(Check.class);
        List<Check> checks = Optional.ofNullable(beansOfType).orElse(Collections.emptyMap())
            .values().stream().collect(Collectors.toList());
        return new VerificationChecksServiceIImpl(checks);
    }
}
