package ru.gostmaster.common.data.verification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * DTO с информацией о результате проверке подписи.
 * 
 * @author maksimgurin 
 */
@Data
public class SignatureInformationResult {
    
    @JsonProperty("signature_check_result_success")
    private boolean signatureCheckResultSuccess = true;

    @JsonProperty("certificate_check_result_success")
    private boolean certificateCheckResultSuccess = true;
    
    @JsonProperty("signature_check_result_description")
    private String signatureCheckResultDescription;

    @JsonProperty("certificate_check_result_description")
    private String certificateCheckResultDescription;

    @JsonProperty("signed_at")
    private Date signedAt;

    @JsonProperty("signature_alg")
    private String signatureAlg;
    @JsonProperty("hashing_alg")
    private String hashingAlg;
    
    private List<SignatureCertificateInfo> signatureCertificateInfo;
}
