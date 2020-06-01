package ru.gostmaster.verification.impl;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.util.BouncyCastleUtils;
import ru.gostmaster.verification.VerificationChecksService;
import ru.gostmaster.verification.VerificationService;
import ru.gostmaster.verification.data.CheckResult;
import ru.gostmaster.verification.data.SignatureCertificateInfo;
import ru.gostmaster.verification.data.SignatureCheckResult;
import ru.gostmaster.verification.data.VerificationResult;
import ru.gostmaster.verification.exception.SignatureVerificationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса проверки на основании  BouncyCastle.
 * 
 * @author maksimgurin 
 */
@Component
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private VerificationChecksService verificationChecksService;

    @Override
    public Mono<VerificationResult> verify(CMSSignedData cmsSignedData) {
        List<Pair<SignerInformation, X509CertificateHolder>> infoWithCerts = getSignerAndCerts(cmsSignedData);
        Mono<List<SignatureCheckResult>> signatureCheckResultFlux = Flux.fromIterable(infoWithCerts)
            .flatMap(pair -> verifySignature(pair.getFirst(), pair.getSecond()))
            .collectList();

        Mono<VerificationResult> res = signatureCheckResultFlux.map(signatureCheckResults -> {
            VerificationResult result = new VerificationResult();
            result.setVerificationTime(new Date());
            result.setSignatures(signatureCheckResults);
            result.setSignaturesCount(signatureCheckResults.size());

            Boolean allStepsSuccess = signatureCheckResults.stream()
                .flatMap(signatureCheckResult -> Optional.ofNullable(signatureCheckResult.getVerificationCheckResults())
                    .orElse(Collections.emptyList()).stream())
                .allMatch(CheckResult::getSuccess);

            result.setQualified(allStepsSuccess);

            return result;
        });
        return res;
    }

    private List<Pair<SignerInformation, X509CertificateHolder>> getSignerAndCerts(CMSSignedData signedData) {
        List<Pair<SignerInformation, X509CertificateHolder>> res = new ArrayList<>();
        Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        for (SignerInformation signerInformation : signers) {
            Collection<X509CertificateHolder> x509Certificates = signedData.getCertificates()
                .getMatches(signerInformation.getSID());
            if (CollectionUtils.isEmpty(x509Certificates)) {
                throw new SignatureVerificationException("Для подписанта " +
                    signerInformation.getSID().toString() + " не найдено сертификата!");
            } else if (x509Certificates.size() > 1) {
                throw new SignatureVerificationException("Для подписанта " +
                    signerInformation.getSID().toString() + " найдено более 1 сертификата!");
            } else {
                X509CertificateHolder certificateHolder = x509Certificates.iterator().next();
                Pair<SignerInformation, X509CertificateHolder> pair = Pair.of(signerInformation, certificateHolder);
                res.add(pair);
            }
        }
        return res;        
    }
    
    private Mono<SignatureCheckResult> verifySignature(SignerInformation signerInformation, 
                                                       X509CertificateHolder holder) {

        Mono<List<CheckResult>> checks = Flux.fromIterable(verificationChecksService.getChecks())
            .flatMap(stepVerification -> {
                if (stepVerification.isEnabled()) {
                    return stepVerification.verify(signerInformation, holder);
                } else {
                    return Mono.empty();
                }
            })
            .collectList();
        
        Mono<SignatureCertificateInfo> certificateInfoMono = Mono.fromCallable(() -> 
            BouncyCastleUtils.buildSignatureCertificateInfo(signerInformation, holder)
        );
        
        return Mono.zip(checks, certificateInfoMono)
            .map(pair -> new SignatureCheckResult(pair.getT1(), pair.getT2()));
    }
    
    @Autowired
    public void setVerificationChecksService(VerificationChecksService verificationChecksService) {
        this.verificationChecksService = verificationChecksService;
    }
}
