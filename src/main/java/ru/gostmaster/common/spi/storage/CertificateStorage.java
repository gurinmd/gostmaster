package ru.gostmaster.common.spi.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.common.data.cert.Certificate;

import java.util.List;

/**
 * Интерфейс для хранилища сертификатов.
 *
 */
public interface CertificateStorage {

    /**
     * Получаем цепочку сертификатов от листа до головного.
     * @param leafSubjectDn subject атрибут листа
     * @return цепочка сертификатов.
     */
    Mono<List<Certificate>> getCertificateChainForLeafSubject(String leafSubjectDn);

    /**
     * Сохранить сертификаты
     * @param certificateFlux сертификаты
     * @return void
     */
    Mono<Void> saveAllCertificates(Flux<Certificate> certificateFlux);

    /**
     * Удалиить все доверенные сертификаты.
     * @return
     */
    Mono<Void> deleteAllTrusted();

    /**
     * Удалить все промежуточные.
     * @return
     */
    Mono<Void> deleteAllIntermediate();
    
    Flux<Certificate> getAll();
}
