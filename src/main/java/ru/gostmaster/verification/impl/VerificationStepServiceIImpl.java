package ru.gostmaster.verification.impl;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.gostmaster.verification.VerificationStepService;
import ru.gostmaster.verification.data.Step;

/**
 * Осуществляем шаги проверки при помощи  bouncy castle.
 * 
 * @author maksimgurin 
 */
@Component
public class VerificationStepServiceIImpl implements VerificationStepService {
    @Override
    public Mono<Step> checkContentStep(SignerInformation signerInformation, X509CertificateHolder certificateHolder) {
        return null;
    }

    @Override
    public Mono<Step> checkKeyUsageStep(SignerInformation signerInformation, X509CertificateHolder certificateHolder) {
        return null;
    }

    @Override
    public Mono<Step> checkCertChainStep(SignerInformation signerInformation, X509CertificateHolder certificateHolder) {
        return null;
    }

    @Override
    public Mono<Step> checkCertChainWithCrlStep(SignerInformation signerInformation, X509CertificateHolder certificateHolder) {
        return null;
    }
}
