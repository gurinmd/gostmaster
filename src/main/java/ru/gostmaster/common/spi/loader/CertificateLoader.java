package ru.gostmaster.common.spi.loader;

import reactor.core.publisher.Flux;
import ru.gostmaster.common.data.cert.Certificate;

/**
 * Интерфейс, который загружает сертификаты.
 * 
 * @author maksimgurin 
 */
public interface CertificateLoader {

    /**
     * Получить поток сертификатов.
     * @return поток сертификатов.
     */
    Flux<Certificate> loadCertificates();
}
