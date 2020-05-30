package ru.gostmaster.common.data.verification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * DTO для результата проверки подписи.
 * 
 * @author maksimgurin 
 */
@Data
public class VerificationResult {
    
    @JsonProperty("verificated_at")
    private Date verificationTime;

    @JsonProperty("signature_check_result_success")
    private boolean signatureCheckResultSuccess = true;

    @JsonProperty("certificate_check_result_success")
    private boolean certificateCheckResultSuccess = true;

    @JsonProperty("signature_check_result_description")
    private String signatureCheckResultDescription;

    @JsonProperty("certificate_check_result_description")
    private String certificateCheckResultDescription;
    
    @JsonProperty("signatures_count")
    private Integer signaturesCount;
    
    @JsonProperty("signatures")
    private List<SignatureInformationResult> results;
}
