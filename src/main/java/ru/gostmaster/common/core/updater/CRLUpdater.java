package ru.gostmaster.common.core.updater;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.common.data.crl.Crl;
import ru.gostmaster.common.spi.loader.CRLLoader;
import ru.gostmaster.common.spi.storage.CRLStorage;

import java.util.List;

public class CRLUpdater {
    private List<CRLLoader> crlLoaders;
    private CRLStorage crlStorage;

    public CRLUpdater(List<CRLLoader> crlLoaders, CRLStorage crlStorage) {
        this.crlLoaders = crlLoaders;
        this.crlStorage = crlStorage;
    }

    public Mono<Void> uploadNewCrls() {
        return crlStorage
            .deleteAllCrls()
            .then(crlStorage.saveAllCrls(loadFromAllLoaders()));
    }
    
    private Flux<Crl> loadFromAllLoaders() {
        Flux<Crl> certificateFlux = Flux.empty();
        for (CRLLoader loader : crlLoaders) {
            certificateFlux = certificateFlux.mergeWith(loader.loadCertificateRevocationLists());
        }
        return certificateFlux;
    }
}
