package ru.gostmaster.common.spi.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.common.data.crl.Crl;

import java.util.List;

public interface CRLStorage {
    
    Flux<Crl> getAllByDownloadedFromOrCa(List<String> urls, List<String> ca);
    
    Flux<Crl> getAll();
    
    Mono<Void> deleteAllCrls();
    
    Mono<Void> saveAllCrls(Flux<Crl> crl);
}
