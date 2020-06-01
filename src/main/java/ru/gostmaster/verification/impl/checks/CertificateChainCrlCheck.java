package ru.gostmaster.verification.impl.checks;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import ru.gostmaster.data.cert.Certificate;
import ru.gostmaster.data.crl.Crl;
import ru.gostmaster.messages.Messages;
import ru.gostmaster.storage.CRLStorage;
import ru.gostmaster.storage.CertificateStorage;
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
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Проверка цепочки сертификатов с учетом CRL.
 *
 * @author maksimgurin
 */
@Component
@Slf4j
public class CertificateChainCrlCheck implements Check {

    private static final String CERTIFICATE_CHAIN_VALID = "Цепочка сертификатов успешно построена " +
        "(с учетом списков отозванных сертификатов)";
    private static final String CERTIFICATE_CHAIN_INVALID = "Ошибка построения цепочки сертификатов. " +
        "Возможно, возникла проблема с одним из промежуточных сертификатов или не найден список отозванных " +
        "сертификатов для одного из промежуточных";

    @Setter(onMethod_ = {@Autowired})
    private CRLStorage crlStorage;

    @Setter(onMethod_ = {@Autowired})
    private CertificateStorage certificateStorage;

    private CertificateFactory certificateFactory;

    public CertificateChainCrlCheck() throws CertificateException, NoSuchProviderException {
        certificateFactory = CertificateFactory.getInstance("X509", BouncyCastleProvider.PROVIDER_NAME);
    }

    @Override
    public Mono<CheckResult> verify(SignerInformation signerInformation, X509CertificateHolder certificateHolder) {
        String authorityKeyIdentified = BouncyCastleUtils.getAuthorityKeyIdentifier(certificateHolder);
        Mono<List<Certificate>> certificates = certificateStorage.getCertificateChainForLeafKey(authorityKeyIdentified);

        Mono<Set<TrustAnchor>> trustAnchorsMono = certificates.map(certs -> buildTrustAnchors(certs));

        Mono<List<X509CRLHolder>> crlListMono = certificates.map(certs -> certs.stream().map(Certificate::getIssuerKey)
            .filter(StringUtils::hasText)
            .collect(Collectors.toList()))
            .map(list -> {
                list.add(BouncyCastleUtils.getAuthorityKeyIdentifier(certificateHolder));
                return list;
            })
            .flatMap(authIds -> crlStorage.getAllByIssuerKeys(authIds).collectList())
            .map(crls -> buildCrlHolderList(crls));

        Mono<CertStore> certStoreMono = Mono.zip(certificates, crlListMono)
            .map(pair -> buildCertStoreForIntermediateAndSignatureCertificate(pair.getT1(),
                certificateHolder, pair.getT2()));

        Mono<CheckResult> resultMono = Mono.zip(trustAnchorsMono, certStoreMono)
            .map(pair -> checkCertificateChain(pair.getT1(), pair.getT2(), certificateHolder));

        return resultMono;
    }

    private CheckResult checkCertificateChain(Set<TrustAnchor> trustAnchors, CertStore certStore,
                                              X509CertificateHolder certificateHolder) {
        CheckResult checkResult = new CheckResult();
        checkResult.setCode(CheckResults.CHECK_CERTIFICATE_CHAIN_WITH_CRL);
        checkResult.setDescription(CheckResults.CHECK_CERTIFICATE_CHAIN_WITH_CRL_DESCRIPTION);
        try {
            CertPathBuilder pathValidator = CertPathBuilder.getInstance("PKIX",
                BouncyCastleProvider.PROVIDER_NAME);

            X509CertSelector targetConstraint = new X509CertSelector();

            targetConstraint.setSubject(certificateHolder.getSubject().getEncoded());

            PKIXBuilderParameters parameters = new PKIXBuilderParameters(trustAnchors, targetConstraint);
            parameters.addCertStore(certStore);
            parameters.setRevocationEnabled(true);

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

    private List<X509CRLHolder> buildCrlHolderList(List<Crl> crls) {
        List<X509CRLHolder> res = new ArrayList<>();
        for (Crl crl : crls) {
            try {
                X509CRL x509CRL = (X509CRL) certificateFactory.generateCRL(new ByteArrayInputStream(crl.getPemData()
                    .getBytes()));
                res.add(new X509CRLHolder(x509CRL.getEncoded()));
            } catch (Exception ex) {
                log.warn("", ex);
            }
        }
        return res;
    }

    private CertStore buildCertStoreForIntermediateAndSignatureCertificate(List<Certificate> certificates,
                                                                           X509CertificateHolder signatureCertificate,
                                                                           List<X509CRLHolder> crls) {
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

        Optional.ofNullable(crls).orElse(Collections.emptyList())
            .forEach(x509CRLHolder -> jcaCertStoreBuilder.addCRL(x509CRLHolder));

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

    @Override
    public boolean isEnabled() {
        return true;
    }
}
