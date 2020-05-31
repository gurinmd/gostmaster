package ru.gostmaster.verification.impl.checks;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.gostmaster.data.cert.Certificate;
import ru.gostmaster.messages.Messages;
import ru.gostmaster.spi.storage.CertificateStorage;
import ru.gostmaster.util.BouncyCastleUtils;
import ru.gostmaster.util.GetterUtils;
import ru.gostmaster.verification.Check;
import ru.gostmaster.verification.data.CheckResult;
import ru.gostmaster.verification.data.CheckResults;

import java.io.ByteArrayInputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Проверка наличия цепочки сертификации без учета списка отозванных сертификатов.
 * 
 * @author maksimgurin 
 */
@Slf4j
@Component
public class CertificateChainCheck implements Check {
    
    private static final String CERTIFICATE_CHAIN_VALID = "Цепочка сертификатов успешно построена " +
        "(без учета  списков отозванных сертификатов)";
    private static final String CERTIFICATE_CHAIN_INVALID = "Ошибка построения цепочки сертификатов";
    
    private CertificateStorage certificateStorage;
    private CertificateFactory certificateFactory;

    public CertificateChainCheck() throws CertificateException, NoSuchProviderException {
        this.certificateFactory = CertificateFactory.getInstance("X509", BouncyCastleProvider.PROVIDER_NAME);
    }
    
    @Override
    public Mono<CheckResult> verify(SignerInformation signerInformation, X509CertificateHolder certificateHolder) {
        String authorityKeyIdentified = BouncyCastleUtils.getAuthorityKeyIdentifier(certificateHolder);
        Mono<List<Certificate>> certificates = certificateStorage.getCertificateChainForLeafKey(authorityKeyIdentified);
        Mono<Set<TrustAnchor>> anchors = certificates.map(certs -> buildTrustAnchors(certs));
        Mono<CertStore> certStoreMono = certificates
            .map(certs -> buildCertStoreForIntermediateAndSignatureCertificate(certs, certificateHolder));

        Mono<CheckResult> res = Mono.zip(anchors, certStoreMono)
            .map(pair -> checkCertificateChain(pair.getT1(), pair.getT2(), certificateHolder));
        
        return res;
    }
    
    private CheckResult checkCertificateChain(Set<TrustAnchor> trustAnchors, CertStore certStore, 
                                              X509CertificateHolder certificateHolder) {
        CheckResult checkResult = new CheckResult();
        checkResult.setCode(CheckResults.CHECK_CERTIFICATE_CHAIN);
        checkResult.setDescription(CheckResults.CHECK_CERTIFICATE_CHAIN_DESCRIPTION);
        try {
            CertPathBuilder pathValidator = CertPathBuilder.getInstance("PKIX", BouncyCastleProvider.PROVIDER_NAME);
            X509CertSelector targetConstraint = new X509CertSelector();
            
            targetConstraint.setSubject(certificateHolder.getSubject().getEncoded());
            
            PKIXBuilderParameters parameters = new PKIXBuilderParameters(trustAnchors, targetConstraint);
            parameters.addCertStore(certStore);
            parameters.setRevocationEnabled(false);

            PKIXCertPathBuilderResult res = (PKIXCertPathBuilderResult) pathValidator.build(parameters);
           
            checkResult.setCreatedAt(new Date());
            checkResult.setResultDescription(CERTIFICATE_CHAIN_VALID);
            checkResult.setSuccess(true);
        } catch (Exception ex) {
            log.warn("", ex);
            checkResult.setSuccess(false);
            checkResult.setCreatedAt(new Date());
            checkResult.setResultDescription(getReadableErrorMessage(ex));
        }
        
        return checkResult;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Autowired
    public void setCertificateStorage(CertificateStorage certificateStorage) {
        this.certificateStorage = certificateStorage;
    }
    
    private Set<TrustAnchor> buildTrustAnchors(List<Certificate> certificates) {
        Set<TrustAnchor> anchors = new HashSet<>();
        for (Certificate certificate : certificates) {
            if (certificate.isTrusted()) {
                try {
                    X509Certificate cert = (X509Certificate) certificateFactory
                        .generateCertificate(new ByteArrayInputStream(certificate.getPemData().getBytes()));
                    TrustAnchor trustAnchor = new TrustAnchor(cert, null);
                    anchors.add(trustAnchor);
                } catch (Exception ex) {
                    log.warn("", ex);
                }
            }
        }
        return anchors;
    } 
    
    private CertStore buildCertStoreForIntermediateAndSignatureCertificate(List<Certificate> certificates, 
                                                                           X509CertificateHolder signatureCertificate) {
        JcaCertStoreBuilder jcaCertStoreBuilder = new JcaCertStoreBuilder();
        jcaCertStoreBuilder.addCertificate(signatureCertificate);
        CertStore certStore = null;
        for (Certificate certificate : certificates) {
            if (!certificate.isTrusted()) {
                try {
                    X509Certificate cert = (X509Certificate) certificateFactory
                        .generateCertificate(new ByteArrayInputStream(certificate.getPemData().getBytes()));
                    jcaCertStoreBuilder.addCertificate(new X509CertificateHolder(cert.getEncoded()));
                } catch (Exception ex) {
                    log.warn("", ex);
                }
            }
        }
        try {
            certStore = jcaCertStoreBuilder.build();
        } catch (Exception e) {
            log.warn("", e);
        }
        return certStore;
    }

    private static String getReadableErrorMessage(Exception ex) {
        String res;
        Throwable cause = GetterUtils.get(() -> ex.getCause().getCause(), new CertificateException());
        if (cause instanceof CertificateExpiredException) {
            res = Messages.getMessage(Messages.CERTIFICATE_EXPIRED);
        } else if (cause instanceof CertificateNotYetValidException) {
            res = Messages.getMessage(Messages.CERTIFICATE_NOT_YET_VALID);
        } else if (cause instanceof CertificateRevokedException) {
            res = Messages.getMessage(Messages.CERTIFICATE_REVOKED);
        } else {
            res = CERTIFICATE_CHAIN_INVALID;
        }
        return res;
    }
}
