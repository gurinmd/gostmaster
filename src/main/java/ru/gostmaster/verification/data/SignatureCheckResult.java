package ru.gostmaster.verification.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * DTO с информацией о результате проверке одной подписи.
 * 
 * @author maksimgurin 
 */
@Data
@AllArgsConstructor
public class SignatureCheckResult {
    
    @JsonProperty("verification_steps")
    private List<CheckResult> verificationCheckResults;
    
    @JsonProperty("signature_certificate_info")
    private SignatureCertificateInfo signatureCertificateInfo;
}
