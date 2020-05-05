package ru.gostmaster.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.gostmaster.common.core.updater.CRLUpdater;
import ru.gostmaster.common.core.updater.CertificateUpdater;
import ru.gostmaster.common.spi.loader.CRLLoader;
import ru.gostmaster.loader.cert.DirectoryCertificateLoader;
import ru.gostmaster.loader.cert.XMLInetCertificateLoader;
import ru.gostmaster.loader.crl.ListCrlLoader;
import ru.gostmaster.loader.crl.XMLInetCrlLoader;
import ru.gostmaster.storage.MongoCertificateStorage;
import ru.gostmaster.storage.MongoCrlStorage;

import java.util.Arrays;
import java.util.List;

@Configuration
public class Config {
    
    @Autowired
    @Bean
    public CertificateUpdater certificateUpdater(XMLInetCertificateLoader inetLoader,
                                                 DirectoryCertificateLoader dirLoader,
                                                 MongoCertificateStorage storage) {
        return new CertificateUpdater(dirLoader, inetLoader, storage);
    }
    
    @Autowired
    @Bean
    public CRLUpdater crlUpdater(XMLInetCrlLoader inetLoader, ListCrlLoader listCrlLoader, MongoCrlStorage storage) {
        List<CRLLoader> loaders = Arrays.asList(listCrlLoader, inetLoader);
        CRLUpdater updater = new CRLUpdater(loaders, storage);
        return updater;
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
