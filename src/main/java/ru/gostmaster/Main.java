package ru.gostmaster;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.security.Security;

/**
 * Запускаем все.
 * 
 * @author maksimgurin 
 */
@SpringBootApplication
@EnableWebFlux
@EnableReactiveMongoRepositories
public class Main {
    
    /**
     * Запуск Spring Context.
     * @param args параметры командной строки
     * @throws Exception если контейнер не смог стартовать.
     */
    @SuppressWarnings("uncommentedmain")
    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        ApplicationContext context = SpringApplication.run(Main.class, args);
//        File sig = new File("/Users/maksimgurin/Downloads/7127021a-7816-4977-a6e2-279257d8fb27.pdf.sig");
//        File data = new File("/Users/maksimgurin/Downloads/7127021a-7816-4977-a6e2-279257d8fb27.pdf");
//        byte[] sigByttes = Files.toByteArray(sig);
//        byte[] dataBytes = Files.toByteArray(data);
//
//        CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(dataBytes), sigByttes);
//        SignerInformation signerInformation = signedData.getSignerInfos().getSigners().stream().findAny().get();
//        signedData.getSignerInfos().getSigners();
//        
//        X509CertificateHolder holder = (X509CertificateHolder) signedData.getCertificates()
//            .getMatches(signerInformation.getSID()).stream().findAny().get();
//
////        CertificateFactory factory = CertificateFactory.getInstance("X509", BouncyCastleProvider.PROVIDER_NAME);
////        X509Certificate certificate = (X509Certificate) factory.generateCertificate(new FileInputStream(cer));
////        X509CertificateHolder holder = new X509CertificateHolder(certificate.getEncoded());
//        SignatureCertificateInfo signatureCertificateInfo = BouncyCastleUtils.fromSignatureCertificateHolder(holder, 
//            signerInformation);
//        System.out.println();
    }
}
