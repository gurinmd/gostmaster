package ru.gostmaster.verification;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformation;
import reactor.core.publisher.Mono;
import ru.gostmaster.verification.data.Step;

/**
 * Класс для выполнения отдельных шагов верификации подписи.
 * 
 * @author maksimgurin 
 */
public interface VerificationStepService {

    /**
     * Шаг, на котором проверяется соответствие подписи и документа.
     * @param signerInformation инфа о подписанте
     * @param certificateHolder соответствующий ему сертификат
     * @return step
     */
    Mono<Step> checkContentStep(SignerInformation signerInformation, X509CertificateHolder certificateHolder);

    /**
     * Шаг, на котором проверяется инфа об использовании сертификата.
     * @param signerInformation инфа о подписанте
     * @param certificateHolder соответствующий ему сертификат
     * @return step
     */
    Mono<Step> checkKeyUsageStep(SignerInformation signerInformation, X509CertificateHolder certificateHolder);

    /**
     * Шаг, на котором проверяется цепочка сертификатов.
     * @param signerInformation инфа о подписанте
     * @param certificateHolder соответствующий ему сертификат
     * @return step
     */
    Mono<Step> checkCertChainStep(SignerInformation signerInformation, X509CertificateHolder certificateHolder);

    /**
     * Шаг, на котором проверяется цепочка сертификатов с учетом CRL.
     * @param signerInformation инфа о подписанте
     * @param certificateHolder соответствующий ему сертификат
     * @return step
     */
    Mono<Step> checkCertChainWithCrlStep(SignerInformation signerInformation, X509CertificateHolder certificateHolder);
}
