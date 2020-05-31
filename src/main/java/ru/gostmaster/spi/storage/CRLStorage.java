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
     * Получить все списки  по ссылкам, по которым обыли размещены, а так же по субъектам, для которых они опубликованы.
     * @param urls ссылки
     * @param ca субъекты (ключи субъектов)
     * @return flux
     */
    Flux<Crl> getAllByDownloadedFromOrCa(List<String> urls, List<String> ca);

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
