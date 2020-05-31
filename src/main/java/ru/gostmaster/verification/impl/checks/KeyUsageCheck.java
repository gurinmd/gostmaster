package ru.gostmaster.verification.impl.checks;

import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.gostmaster.verification.Check;
import ru.gostmaster.verification.data.CheckResult;
import ru.gostmaster.verification.data.CheckResults;

import java.util.Date;

/**
 * Проверка корректности выставления  флагов KeyUsage.
 * 
 * @author maksimgurin 
 */
@Component
public class KeyUsageCheck implements Check {
    
    @Override
    public Mono<CheckResult> verify(SignerInformation signerInformation, X509CertificateHolder certificateHolder) {
        KeyUsage keyUsage = KeyUsage.fromExtensions(certificateHolder.getExtensions());
        boolean signature = keyUsage.hasUsages(KeyUsage.digitalSignature);
        boolean nonRepudiation = keyUsage.hasUsages(KeyUsage.nonRepudiation);
        boolean keyEncipherment = keyUsage.hasUsages(KeyUsage.keyEncipherment);
        boolean dataEncipherment = keyUsage.hasUsages(KeyUsage.dataEncipherment);

        boolean res = signature && nonRepudiation && keyEncipherment && dataEncipherment;
        String message;
        if (res) {
            message = String.format("В сертификате подписи выставлены флаги keyUsage: Цифровая подпись, " +
                "Неотрекаемость, Шифрование ключей, Шифрование данных");
        } else {
            message = String.format("В сертификате подписи в keyUsage выставлены не все необходимые флаги. " +
                "Состояние флагов: Цифровая подпись [%s], Неотрекаемость [%s], Шифрование ключей [%s], " +
                "Шифрование данных [%s]", signature, nonRepudiation, keyEncipherment, dataEncipherment);
        }
        CheckResult checkResult = new CheckResult();
        checkResult.setCode(CheckResults.CHECK_KEY_USAGE);
        checkResult.setDescription(CheckResults.CHECK_KEY_USAGE_DESCRIPTION);
        checkResult.setCreatedAt(new Date());
        checkResult.setSuccess(res);
        checkResult.setResultDescription(message);
        return Mono.just(checkResult);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
