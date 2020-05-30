package ru.gostmaster.common.reactor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import ru.gostmaster.common.data.crl.Crl;
import ru.gostmaster.common.download.FileDownloadHelper;
import ru.gostmaster.parser.CrlParser;

/**
 * Класс с утилитными методами для загрузки CRL.
 * 
 * @author maksimgurin 
 */
@Component
@Slf4j
public class CrlFluxHelper {
    private CrlParser crlParser;
    private FileDownloadHelper fileDownloadHelper;

    /**
     * Загрузки CRL и получение их в виде потока.
     * @param urls поток со ссылками
     * @return потом CRL
     */
    public Flux<Crl> getCrlFluxFromUrls(Flux<String> urls) {
        return urls
            .parallel()
            .runOn(Schedulers.newElastic("crl-download-thread-pool"))
            .flatMap(fileDownloadHelper::download)
            .filter(stringPair -> stringPair.getSecond().length > 0)
            .map(stringPair -> crlParser.parseRawDataCrl(stringPair.getSecond(), stringPair.getFirst()))
            .sequential()
            .onErrorContinue((throwable, o) -> {
                log.error("Error processing url " + o, throwable.getMessage());
            });
    }

    @Autowired
    public void setCrlParser(CrlParser crlParser) {
        this.crlParser = crlParser;
    }

    @Autowired
    public void setFileDownloadHelper(FileDownloadHelper fileDownloadHelper) {
        this.fileDownloadHelper = fileDownloadHelper;
    }
}
