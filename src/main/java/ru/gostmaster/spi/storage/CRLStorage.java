package ru.gostmaster.spi.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.data.crl.Crl;

import java.util.List;

/**
 * Интерфейс хранилища списка отозванных сертификатов (СRL).
 * 
 * @author maksimgurin 
 */
public interface CRLStorage {

    /**
     * Получить все списки по субъектам, для которых они опубликованы.
     * @param issuerKeys субъекты
     * @return flux
     */
    Flux<Crl> getAllByIssuerKeys(List<String> issuerKeys);

    /**
     * Получить все списки.
     * @return flux
     */
    Flux<Crl> getAll();

    /**
     * Удалить все списки.
     * @return mono
     */
    Mono<Void> deleteAllCrls();

    /**
     * Сохранить все списки из источника.
     * @param crl источник
     * @return mono
     */
    Mono<Void> saveAllCrls(Flux<Crl> crl);
}
