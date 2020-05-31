package ru.gostmaster.verification;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformation;
import reactor.core.publisher.Mono;
import ru.gostmaster.verification.data.CheckResult;

/**
 * Интерфейс одной конкретной проверки подписи. 
 * 
 * @author maksimgurin 
 */
public interface Check {
    
    /**
     * Провести какую то конкретную проверку подписи.
     * @param signerInformation подписант
     * @param certificateHolder сертификат
     * @return результат проверки
     */
    Mono<CheckResult> verify(SignerInformation signerInformation, X509CertificateHolder certificateHolder);

    /**
     * Активна ли проверка или нет.
     * @return флаг
     */
    boolean isEnabled();
}
