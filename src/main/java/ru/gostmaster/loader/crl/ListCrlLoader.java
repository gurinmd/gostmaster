package ru.gostmaster.loader.crl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.gostmaster.data.crl.Crl;
import ru.gostmaster.reactor.CrlFluxHelper;
import ru.gostmaster.spi.loader.CRLLoader;
import ru.gostmaster.util.FileUtils;

/**
 * Компонент-загрузчик CRL из файла со списком URL.
 * 
 * @author maksimgurin 
 */
@Component
@Slf4j
public class ListCrlLoader implements CRLLoader {

    private String crlUrlsListFile;
    private CrlFluxHelper crlFluxHelper;
    
    @Override
    public Flux<Crl> loadCertificateRevocationLists() {
        log.info("Loading all crl from url list file {}", crlUrlsListFile);
        Flux<String> urls = FileUtils.fileLines(crlUrlsListFile);
        Flux<Crl> res = crlFluxHelper.getCrlFluxFromUrls(urls);
        return res;
    }

    @Value("${crl.data.url-list}")
    public void setCrlUrlsListFile(String crlUrlsListFile) {
        this.crlUrlsListFile = crlUrlsListFile;
    }

    @Autowired
    public void setCrlFluxHelper(CrlFluxHelper crlFluxHelper) {
        this.crlFluxHelper = crlFluxHelper;
    }
}
