package ru.gostmaster.common.data.verification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class SignatureCertificateInfo {
    private CertificateSubject subject;
    private CertificateSubject issuer;
    
    @JsonProperty("valid_from")
    private Date validFrom;
    @JsonProperty("valid_to")
    private Date validTo;
}
