package ru.gostmaster.common.core.updater;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.common.data.cert.Certificate;
import ru.gostmaster.common.spi.loader.CertificateLoader;
import ru.gostmaster.common.spi.storage.CertificateStorage;

@Slf4j
public class CertificateUpdater {
    
    private CertificateLoader trustedCertificateLoader;
    private CertificateLoader intermediateCertificateLoader;
    private CertificateStorage certificateStorage;

    public CertificateUpdater(CertificateLoader trustedCertificateLoader,
                              CertificateLoader intermediateCertificateLoader,
                              CertificateStorage certificateStorage) {
        this.trustedCertificateLoader = trustedCertificateLoader;
        this.intermediateCertificateLoader = intermediateCertificateLoader;
        this.certificateStorage = certificateStorage;
    }

    public Mono<Void> uploadNewTrustedCertificates() {
        Flux<Certificate> certificatesToLoad = loadTrustedCertificates();
        return certificateStorage.deleteAllTrusted()
            .then(certificateStorage.saveAllCertificates(certificatesToLoad));
    }

    public Mono<Void> uploadNewIntermediateCertificates() {
        Flux<Certificate> certificatesToLoad = loadIntermediateCertificates();
        return certificateStorage.deleteAllIntermediate()
            .then(certificateStorage.saveAllCertificates(certificatesToLoad));
    }
    
    private Flux<Certificate> loadIntermediateCertificates() {
        return intermediateCertificateLoader
            .loadCertificates()
            .filter(certificate -> !certificate.isTrusted());
    }

    private Flux<Certificate> loadTrustedCertificates() {
        return trustedCertificateLoader
            .loadCertificates()
            .filter(Certificate::isTrusted);
    }
}
