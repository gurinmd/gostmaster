package ru.gostmaster.updater;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Компонент, который занимается обновлением всех данных. Алгоритм следующий:
 * 1. Загружаются все сертификаты. Параллельно извлекаются ссылки на CRL и складываются в хранилище ссылок.
 * 2. Из дополнительных источников извлекаются ссылки на CRL и сохраняются в  хранилище ссылок.
 * 3. Идем по хранилищу ссылок. Если какая-то ссылка не  скачана, или же надо обновить - пытаемся скачать.
 *
 * @author maksimgurin
 */
@Component
@Slf4j
public class DataUpdater {

    @Setter(onMethod_ = {@Autowired})
    private CertificateUpdater certificateUpdater;

    @Setter(onMethod_ = {@Autowired})
    private CRLUrlUpdater crlUrlUpdater;

    @Setter(onMethod_ = {@Autowired})
    private CRLUpdater crlUpdater;

    /**
     * Центральный метод, который запускается при обновлении данных.
     *
     * @return void
     */
    public Mono<Void> doUpdate() {
        //1. Загружаем сертификаты
        Mono<Void> certificateUploadedMono = Mono.defer(() -> certificateUpdater.uploadNewTrustedCertificates())
            .then(certificateUpdater.uploadNewIntermediateCertificates())
            .doFinally(signalType -> {
                log.info("**************************");
                log.info("* Сертификаты загружены! *");
                log.info("**************************");
            }).cache();

        //2. Загружаем дополнительные ссылки на CRL.
        Mono<Void> crlUrlUploadedMono = crlUrlUpdater.uploadNewCrlUrls()
            .doFinally(signalType -> {
                log.info("********************************************************");
                log.info("* Ссылки на списки отозванных сертификатов обнолвлены! *");
                log.info("********************************************************");
            }).cache();

        //3. Загружаем CRL. После того, как были загружены сертификаты и ссылки
        Mono<Void> updatedCrls = certificateUploadedMono
            .then(crlUrlUploadedMono).then(crlUpdater.updateCrls())
            .doFinally(signalType -> {
                log.info("********************************************************************");
                log.info("* Данные сертификатов и списков отозванных сертификатов обновлены! *");
                log.info("********************************************************************");
            });
        return updatedCrls;
    }

    /**
     * Обновление по расписанию.
     */
    @Scheduled(cron = "${data.update.cron.expression}")
    public void doScheduledUpdate() {
        doUpdate().subscribeOn(Schedulers.newElastic("data-update-scheduler")).subscribe();
    }
}
