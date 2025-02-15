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
    
    @JsonProperty("qualified")
    private Boolean qualified;
    
    @JsonProperty("uploading_error")
    private Boolean uploadingError;

    @JsonProperty("uploading_error_description")
    private String uploadingErrorDescription;

    @JsonProperty("uploading_error_date")
    private Date uploadingErrorDate;
}
