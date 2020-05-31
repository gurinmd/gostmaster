package ru.gostmaster.core.updater;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.data.cert.Certificate;
import ru.gostmaster.spi.loader.CertificateLoader;
import ru.gostmaster.spi.storage.CertificateStorage;

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

    /**
     * Конструктор.
     * @param trustedCertificateLoaders загрузчики доверенных сертификатов
     * @param intermediateCertificateLoaders загрузчики промежуточных сертификатов
     * @param certificateStorage хранилище сертификатов
     */
    public CertificateUpdater(List<CertificateLoader> trustedCertificateLoaders, 
                              List<CertificateLoader> intermediateCertificateLoaders, 
                              CertificateStorage certificateStorage) {
        this.trustedCertificateLoaders = trustedCertificateLoaders;
        this.intermediateCertificateLoaders = intermediateCertificateLoaders;
        this.certificateStorage = certificateStorage;
    }

    /**
     * Обновить доверенные сертификаты.
     * @return void
     */
    public Mono<Void> uploadNewTrustedCertificates() {
        Flux<Certificate> certificatesToLoad = loadTrustedCertificates();
        return certificateStorage.deleteAllTrusted()
            .then(certificateStorage.saveAllCertificates(certificatesToLoad));
    }

    /**
     * Обновить промежуточные сертификаты.
     * @return void
     */
    public Mono<Void> uploadNewIntermediateCertificates() {
        Flux<Certificate> certificatesToLoad = loadIntermediateCertificates();
        return certificateStorage.deleteAllIntermediate()
            .then(certificateStorage.saveAllCertificates(certificatesToLoad));
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
