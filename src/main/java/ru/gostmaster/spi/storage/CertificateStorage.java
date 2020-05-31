package ru.gostmaster.spi.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.data.cert.Certificate;

import java.util.List;

/**
 * Интерфейс для хранилища сертификатов.
 *
 * @author maksimgurin 
 */
public interface CertificateStorage {

    /**
     * Получаем цепочку сертификатов от листа до головного.
     * @param subjectKey идентификатор сертификата - листа
     * @return цепочка сертификатов.
     */
    Mono<List<Certificate>> getCertificateChainForLeafKey(String subjectKey);

    /**
     * Сохранить сертификаты.
     * @param certificateFlux сертификаты
     * @return void
     */
    Mono<Void> saveAllCertificates(Flux<Certificate> certificateFlux);

    /**
     * Удалиить все доверенные сертификаты.
     * @return void
     */
    Mono<Void> deleteAllTrusted();

    /**
     * Удалить все промежуточные.
     * @return void
     */
    Mono<Void> deleteAllIntermediate();

    /**
     * Получить все сертификаты.
     * @return сертификаты
     */
    Flux<Certificate> getAll();
}
