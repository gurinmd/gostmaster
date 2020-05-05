package ru.gostmaster;

import com.oracle.tools.packager.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.blockhound.BlockHound;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ru.gostmaster.common.core.updater.CRLUpdater;
import ru.gostmaster.common.core.updater.CertificateUpdater;
import ru.gostmaster.common.core.verificator.SignatureVerificator;
import ru.gostmaster.common.data.crl.Crl;
import ru.gostmaster.common.data.verification.VerificationResult;
import ru.gostmaster.common.spi.loader.CRLLoader;
import ru.gostmaster.common.spi.loader.CertificateLoader;
import ru.gostmaster.common.spi.storage.CRLStorage;
import ru.gostmaster.loader.cert.XMLInetCertificateLoader;
import ru.gostmaster.loader.crl.XMLInetCrlLoader;

import java.io.File;
import java.security.Security;

@SpringBootApplication
@EnableWebFlux
@EnableReactiveMongoRepositories
public class Main {
    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        BlockHound.builder()
            .allowBlockingCallsInside(XMLInetCertificateLoader.class.getName(), "loadCertificates")
            .install();
        ApplicationContext context = SpringApplication.run(Main.class, args);



        
        long start = System.currentTimeMillis();
        File sig = new File("/Users/maksimgurin/Downloads/7127021a-7816-4977-a6e2-279257d8fb27.pdf.sig");
        File data = new File("/Users/maksimgurin/Downloads/7127021a-7816-4977-a6e2-279257d8fb27.pdf");
        //  File data = new File("/Users/maksimgurin/Downloads/TSLExt.1.0.xml");

        byte[] sigBytes = IOUtils.readFully(sig);
        byte[] dataBytes = IOUtils.readFully(data);

        SignatureVerificator verificator = context.getBean(SignatureVerificator.class);
        VerificationResult result = verificator.verify(dataBytes, sigBytes).block();
        System.out.printf("");
    }
}
