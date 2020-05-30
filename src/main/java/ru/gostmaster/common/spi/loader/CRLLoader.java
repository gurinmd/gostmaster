package ru.gostmaster.common.spi.loader;

import reactor.core.publisher.Flux;
import ru.gostmaster.common.data.crl.Crl;

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
