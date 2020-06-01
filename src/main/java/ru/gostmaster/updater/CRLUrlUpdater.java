package ru.gostmaster.updater;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.loader.CRLUrlLoader;
import ru.gostmaster.storage.CRLUrlStorage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс, который занимается обновлением списка отозванных сертификатов (CRL) в хранилище.
 *
 * @author maksimgurin
 */
@Slf4j
public class CRLUrlUpdater {
    private List<CRLUrlLoader> crlUrlLoaders;
    private CRLUrlStorage crlUrlStorage;

    /**
     * Конструктор.
     *
     * @param crlUrlLoaders список загрузчиков CRL
     * @param crlUrlStorage хранилище ссылок на CRL
     */
    public CRLUrlUpdater(List<CRLUrlLoader> crlUrlLoaders, CRLUrlStorage crlUrlStorage) {
        this.crlUrlLoaders = crlUrlLoaders;
        this.crlUrlStorage = crlUrlStorage;
    }

    /**
     * Обновить ссылки на CRL.
     *
     * @return void
     */
    public Mono<Void> uploadNewCrlUrls() {
        return loadFromAllLoaders()
            .flatMap(url -> crlUrlStorage.add(url))
            .then();
    }

    private Flux<String> loadFromAllLoaders() {
        List<Flux<String>> allCrlFlux = crlUrlLoaders.stream().map(CRLUrlLoader::loadCrlUrls)
            .collect(Collectors.toList());
        Flux<String> certificateFlux = Flux.merge(allCrlFlux);
        return certificateFlux;
    }
}
