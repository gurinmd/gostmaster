package ru.gostmaster.verification.data;

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

    @JsonProperty("signatures_count")
    private Integer signaturesCount;
    
    @JsonProperty("signatures")
    private List<SignatureCheckResult> signatures;
    
    @JsonProperty("qualification_status")
    private Boolean qualificationStatus;
}
