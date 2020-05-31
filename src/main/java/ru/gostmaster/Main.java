package ru.gostmaster;

import com.google.common.io.Files;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;
import ru.gostmaster.verification.VerificationService;
import ru.gostmaster.verification.data.VerificationResult;

import java.io.File;
import java.io.FileReader;
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
        File sig = new File("/Users/maksimgurin/Downloads/Re__ФОрмат_sig/госпошлина.pdf.sig");
        File data = new File("/Users/maksimgurin/Downloads/Сущий_крауд__как_бизнес_спасается_в_" +
            "кризис_сбором_средств_в_интернете.pdf");
        
        //context.getBean(CRLUpdater.class).uploadNewCrls().block();
//        
        
        byte[] sigByttes = Files.toByteArray(sig);
        byte[] dataBytes = Files.toByteArray(data);
        PemReader pemReader = new PemReader(new FileReader(sig));
        PemObject pemObject = pemReader.readPemObject();
        System.out.println();
        
//  
        CMSSignedData signedData = new CMSSignedData(pemObject.getContent());
        //CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(dataBytes), sigByttes);
        VerificationService service = context.getBean(VerificationService.class);
        VerificationResult res = service.verify(signedData).block();
        System.out.println();
        SignerInformation signerInformation = signedData.getSignerInfos().getSigners().stream().findAny().get();
        signedData.getSignerInfos().getSigners();

        X509CertificateHolder holder = (X509CertificateHolder) signedData.getCertificates()
            .getMatches(signerInformation.getSID()).stream().findAny().get();

//        CertificateFactory factory = CertificateFactory.getInstance("X509", BouncyCastleProvider.PROVIDER_NAME);
//        X509Certificate certificate = (X509Certificate) factory.generateCertificate(new FileInputStream(cer));
//        X509CertificateHolder holder = new X509CertificateHolder(certificate.getEncoded());
        System.out.println();
    }
}
