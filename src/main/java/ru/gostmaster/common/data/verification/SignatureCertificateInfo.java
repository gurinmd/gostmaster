package ru.gostmaster.common.data.verification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * DTO для информации о сертификате.
 * 
 * @author maksimgurin 
 */
@Data
public class SignatureCertificateInfo {
    private CertificateSubject subject;
    private CertificateSubject issuer;
    
    @JsonProperty("valid_from")
    private Date validFrom;
    @JsonProperty("valid_to")
    private Date validTo;
    
    @JsonProperty("key_usage")
    private List<CertificateKeyUsage> keyUsage;

    @JsonProperty("signature_algorithm")
    private AlgorithmDescription signatureAlgorithm;

    @JsonProperty("hash_algorithm")
    private AlgorithmDescription hashAlgorithm;
}
