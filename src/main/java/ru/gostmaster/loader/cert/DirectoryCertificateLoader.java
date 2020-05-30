package ru.gostmaster.loader.cert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.gostmaster.common.data.cert.Certificate;
import ru.gostmaster.common.spi.loader.CertificateLoader;
import ru.gostmaster.parser.CertificateParser;
import ru.gostmaster.util.FileUtils;

import java.util.function.Function;

/**
 * Компонент-загрузчик сертификатов из директории.
 * 
 * @author maksimgurin 
 */
@Component
@Slf4j
public class DirectoryCertificateLoader implements CertificateLoader {
    
    private String certificateFilePath;
    private CertificateParser parser;
    private boolean trusted = true;

    @Override
    public Flux<Certificate> loadCertificates() {
        Flux<String> fileNamesFlux = FileUtils.listFiles(certificateFilePath, "");
        Flux<byte[]> fileContent = fileNamesFlux.flatMap(s -> FileUtils.fileContent(s));
        Flux<Certificate> res = fileContent.map(bytes -> {
            try {
                Certificate certificateData = parser.parseRawDataCertificate(bytes);
                return certificateData;
            } catch (Exception ex) {
                log.error("", ex.getMessage());
                throw new RuntimeException(ex);
            }
        }).map(Function.identity()).onErrorContinue((throwable, o) -> {
            log.error("", throwable);
        });
        
        return res;
    }

    @Autowired
    public void setParser(CertificateParser parser) {
        this.parser = parser;
    }

    @Value("${cert.trusted.directory}")
    public void setCertificateFilePath(String certificateFilePath) {
        this.certificateFilePath = certificateFilePath;
    }
}
