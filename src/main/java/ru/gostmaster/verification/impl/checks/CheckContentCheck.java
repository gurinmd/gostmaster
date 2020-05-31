package ru.gostmaster.verification.impl.checks;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.gostmaster.messages.Messages;
import ru.gostmaster.verification.Check;
import ru.gostmaster.verification.data.CheckResult;
import ru.gostmaster.verification.data.CheckResults;

import java.util.Date;

/**
 * Проверка соответствия подписи подписанному содержимому.
 *
 * @author maksimgurin
 */
@Component
@Slf4j
public class CheckContentCheck implements Check {
    
    @Override
    public Mono<CheckResult> verify(SignerInformation signerInformation, X509CertificateHolder certificateHolder) {
        CheckResult checkResult = new CheckResult();
        checkResult.setCode(CheckResults.CHECK_SIGN_CONTENT);
        checkResult.setDescription(CheckResults.CHECK_SIGN_CONTENT_DESCRIPTION);
        boolean res;
        String message;
        try {
            res = signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder()
                .build(certificateHolder));
            if (res) {
                message = Messages.getMessage(Messages.VALID_SIGNED_CONTENT);
            } else {
                message = Messages.getMessage(Messages.INVALID_SIGNED_CONTENT);
            }
        } catch (Exception exception) {
            log.warn("", exception);
            message = Messages.getMessage(Messages.INVALID_SIGNED_CONTENT);
            res = false;
        }
        checkResult.setSuccess(res);
        checkResult.setResultDescription(message);
        checkResult.setCreatedAt(new Date());
        return Mono.just(checkResult);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
