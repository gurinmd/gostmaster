package ru.gostmaster.loader;

import reactor.core.publisher.Flux;
import ru.gostmaster.data.cert.Certificate;

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
