package ru.gostmaster.common.spi.loader;

import reactor.core.publisher.Flux;
import ru.gostmaster.common.data.crl.Crl;

public interface CRLLoader {
    Flux<Crl> loadCertificateRevocationLists();
}
