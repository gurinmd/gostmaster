package ru.gostmaster.updater;

import lombok.Setter;
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
    @Scheduled(cron = "* * */4 * * *")
    public Mono<Void> doUpdate() {
        //1. Загружаем сертификаты
        Mono<Void> certificateUploadedMono = certificateUpdater.uploadNewTrustedCertificates()
            .then(certificateUpdater.uploadNewIntermediateCertificates())
            .checkpoint("Сертификаты загружены");

        //2. Загружаем дополнительные ссылки на CRL.
        Mono<Void> crlUrlUploadedMono = crlUrlUpdater.uploadNewCrlUrls()
            .checkpoint("Ссылки на CRL извлечены");

        //3. Загружаем CRL. После того, как были загружены сертификаты и ссылки
        Mono<Void> updatedCrls = certificateUploadedMono.then(crlUrlUploadedMono).then(crlUpdater.updateCrls())
            .checkpoint("CRL обновлены")
            .subscribeOn(Schedulers.newElastic("data_updating_scheduler"));
        return updatedCrls;
    }
}
