package ru.gostmaster.verification;

import org.bouncycastle.cms.CMSSignedData;
import reactor.core.publisher.Mono;
import ru.gostmaster.verification.data.VerificationResult;

/**
 * Cервис верификации подписанного документа.
 * 
 * @author maksimgurin 
 */
public interface VerificationService {

    /**
     * Верифицируем подписанный документ.
     * @param cmsSignedData подписанные данные.
     * @return результат
     */
    Mono<VerificationResult> verify(CMSSignedData cmsSignedData);
}
