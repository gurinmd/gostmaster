package ru.gostmaster.updater;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.gostmaster.data.crl.CrlUrl;
import ru.gostmaster.reactor.CrlFluxHelper;
import ru.gostmaster.storage.CRLStorage;
import ru.gostmaster.storage.CRLUrlStorage;

/**
 * Обновляем CRL на основании CRLUrlStorage.
 *
 * @author maksimgurin
 */
@Slf4j
@Component
public class CRLUpdater {

    @Setter(onMethod_ = {@Autowired})
    private CRLUrlStorage crlUrlStorage;
    @Setter(onMethod_ = {@Autowired})
    private CrlFluxHelper crlFluxHelper;
    @Setter(onMethod_ = {@Autowired})
    private CRLStorage crlStorage;

    /**
     * Обновляем CRL на основании CRLUrlStorage.
     *ч
     * @return void
     */
    public Mono<Void> updateCrls() {
        Flux<CrlUrl> urls = crlUrlStorage.getAll();
//        Flux<CrlUrl> urls = crlUrlStorage.getAll().filter(crlUrl ->
//            (crlUrl.getLastUpdate() == null && crlUrl.getNextUpdate() == null) ||
//                crlUrl.getNextUpdate().compareTo(new Date()) < 0
//        );

        Mono<Void> res = urls.parallel().runOn(Schedulers.newParallel("crl-download-thread-pool"))
            .flatMap(crlUrl -> crlFluxHelper.getCrlFromUrl(crlUrl.getUrl()))
            .sequential()
            .onErrorContinue((throwable, o) -> log.debug("Error downloading from " + o, throwable.getMessage()))
            .parallel()
            .runOn(Schedulers.newElastic("crl-save-thread-pool"))
            .flatMap(crl -> crlStorage.save(crl))
            .flatMap(crl -> crlUrlStorage.update(crl))
            .then();
        return res;
    }
} 
