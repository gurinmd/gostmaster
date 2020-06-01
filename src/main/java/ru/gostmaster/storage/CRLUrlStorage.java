package ru.gostmaster.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.data.crl.Crl;
import ru.gostmaster.data.crl.CrlUrl;

/**
 * Интерфейс для хранилища ссылок на CRL.
 *
 * @author maksimgurin
 */
public interface CRLUrlStorage {

    /**
     * Добавляем ссылку. Если такая уже есть - ничего не делаем.
     *
     * @param url ссылка
     * @return void
     */
    Mono<Void> add(String url);

    /**
     * Получить объект по ссылке.
     *
     * @param url ссылка
     * @return объект (1 или несколько)
     */
    Flux<CrlUrl> getByUrl(String url);

    /**
     * Получить все объекты из БД.
     *
     * @return поток обхектов
     */
    Flux<CrlUrl> getAll();

    /**
     * Обновить данные о ссылке.
     *
     * @param crl скачанная и загруженная crl
     * @return void
     */
    Mono<Void> update(Crl crl);
}
