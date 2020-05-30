package ru.gostmaster.common.core.updater;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.common.data.crl.Crl;
import ru.gostmaster.common.spi.loader.CRLLoader;
import ru.gostmaster.common.spi.storage.CRLStorage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс, который занимается обновлением списка отозванных сертификатов (CRL) в хранилище.
 *
 * @author maksimgurin
 */
@Slf4j
public class CRLUpdater {
    private List<CRLLoader> crlLoaders;
    private CRLStorage crlStorage;

    /**
     * Конструктор.
     * @param crlLoaders список загрузчиков CRL
     * @param crlStorage хранилище CRL
     */
    public CRLUpdater(List<CRLLoader> crlLoaders, CRLStorage crlStorage) {
        this.crlLoaders = crlLoaders;
        this.crlStorage = crlStorage;
    }

    /**
     * Обновить CRL.
     * @return void
     */
    public Mono<Void> uploadNewCrls() {
        return crlStorage
            .deleteAllCrls()
            .then(crlStorage.saveAllCrls(loadFromAllLoaders()));
    }
    
    private Flux<Crl> loadFromAllLoaders() {
        List<Flux<Crl>> allCrlFlux = crlLoaders.stream().map(CRLLoader::loadCertificateRevocationLists)
            .collect(Collectors.toList());
        Flux<Crl> certificateFlux = Flux.merge(allCrlFlux);
        return certificateFlux;
    }
}
