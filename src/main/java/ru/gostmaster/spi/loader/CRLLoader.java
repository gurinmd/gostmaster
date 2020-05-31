package ru.gostmaster.spi.loader;

import reactor.core.publisher.Flux;
import ru.gostmaster.data.crl.Crl;

/**
 * Интерфейс загрузчика списков отозванных сертификатов CRL.
 * 
 * @author maksimgurin 
 */
public interface CRLLoader {

    /**
     * Загрузить списки.
     * @return поток списков
     */
    Flux<Crl> loadCertificateRevocationLists();
}
