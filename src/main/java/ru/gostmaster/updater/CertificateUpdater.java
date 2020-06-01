package ru.gostmaster.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.data.cert.Certificate;
import ru.gostmaster.loader.CertificateLoader;
import ru.gostmaster.storage.CRLUrlStorage;
import ru.gostmaster.storage.CertificateStorage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс, который занимается обновлением списка сертификатов в хранилище.
 * 
 * @author maksimgurin 
 */
@Slf4j
public class CertificateUpdater {
    
    private List<CertificateLoader> trustedCertificateLoaders;
    private List<CertificateLoader> intermediateCertificateLoaders;
    private CertificateStorage certificateStorage;
    private CRLUrlStorage crlUrlStorage;

    /**
     * Конструктор.
     *
     * @param trustedCertificateLoaders      загрузчики доверенных сертификатов
     * @param intermediateCertificateLoaders загрузчики промежуточных сертификатов
     * @param certificateStorage             хранилище сертификатов
     */
    public CertificateUpdater(List<CertificateLoader> trustedCertificateLoaders,
                              List<CertificateLoader> intermediateCertificateLoaders,
                              CertificateStorage certificateStorage,
                              CRLUrlStorage crlUrlStorage) {
        this.trustedCertificateLoaders = trustedCertificateLoaders;
        this.intermediateCertificateLoaders = intermediateCertificateLoaders;
        this.certificateStorage = certificateStorage;
        this.crlUrlStorage = crlUrlStorage;
    }

    /**
     * Обновить доверенные сертификаты.
     * @return void
     */
    public Mono<Void> uploadNewTrustedCertificates() {
        Flux<Certificate> certificatesToLoad = loadTrustedCertificates();

        Mono<Void> savedCrls = certificatesToLoad.flatMapIterable(certificate -> certificate.getCrlUrls())
            .filter(StringUtils::hasText)
            .flatMap(s -> crlUrlStorage.add(s))
            .then();

        // не будем удалять сертификаты. будем их перетирать.
        return certificateStorage.saveAllCertificates(certificatesToLoad)
            .then(savedCrls);
    }

    /**
     * Обновить промежуточные сертификаты.
     * @return void
     */
    public Mono<Void> uploadNewIntermediateCertificates() {
        Flux<Certificate> certificatesToLoad = loadIntermediateCertificates();
        Mono<Void> savedCrls = certificatesToLoad.flatMapIterable(certificate -> certificate.getCrlUrls())
            .filter(StringUtils::hasText)
            .flatMap(s -> crlUrlStorage.add(s))
            .then();
        // не будем удалять сертификаты. будем их перетирать.
        return certificateStorage.saveAllCertificates(certificatesToLoad)
            .then(savedCrls);
    }
    
    private Flux<Certificate> loadIntermediateCertificates() {
        List<Flux<Certificate>> allIntermCers = intermediateCertificateLoaders
            .stream().map(CertificateLoader::loadCertificates).collect(Collectors.toList());
        return Flux.merge(allIntermCers)
            .map(certificate -> {
                certificate.setTrusted(false);
                return certificate;
            });
    }

    private Flux<Certificate> loadTrustedCertificates() {
        List<Flux<Certificate>> allTrustedCerts = trustedCertificateLoaders
            .stream().map(CertificateLoader::loadCertificates).collect(Collectors.toList());
        return Flux.merge(allTrustedCerts)
            .map(certificate -> {
                certificate.setTrusted(true);
                return certificate;
            });
    }
}
