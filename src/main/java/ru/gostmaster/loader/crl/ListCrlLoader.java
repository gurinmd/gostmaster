package ru.gostmaster.loader.crl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.gostmaster.common.reactor.FluxHelper;
import ru.gostmaster.common.data.crl.Crl;
import ru.gostmaster.common.spi.loader.CRLLoader;
import ru.gostmaster.util.FileUtils;

@Component
@Slf4j
public class ListCrlLoader implements CRLLoader {

    private String crlUrlsListFile;
    private FluxHelper fluxHelper;


    @Override
    public Flux<Crl> loadCertificateRevocationLists() {
        log.info("Loading all crl from url list file {}", crlUrlsListFile);
        Flux<String> urls = FileUtils.fileLines(crlUrlsListFile);
        Flux<Crl> res = fluxHelper.getCrlFluxFromUrls(urls);
        return res;
    }

    @Value("${crl.data.url-list}")
    public void setCrlUrlsListFile(String crlUrlsListFile) {
        this.crlUrlsListFile = crlUrlsListFile;
    }

    @Autowired
    public void setFluxHelper(FluxHelper fluxHelper) {
        this.fluxHelper = fluxHelper;
    }
}
