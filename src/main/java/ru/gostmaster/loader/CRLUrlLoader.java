package ru.gostmaster.loader;

import reactor.core.publisher.Flux;

/**
 * Интерфейс загрузчика списков отозванных сертификатов CRL.
 *
 * @author maksimgurin
 */
public interface CRLUrlLoader {

    /**
     * Загрузить ссылки на списки.
     *
     * @return поток ссылок
     */
    Flux<String> loadCrlUrls();
}
