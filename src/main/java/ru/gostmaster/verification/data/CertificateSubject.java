package ru.gostmaster.verification.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO для огранизации издателя или владельца сертификата.
 * 
 * @author maksimgurin 
 */
@Data
public class CertificateSubject {
    
    private String organization;
    
    @JsonProperty("organization_unit")
    private String organizationUnit;
    
    private String email;
    
    private String title;
    
    @JsonProperty("common_name")
    private String commonName;
    
    private String country;
    
    private String state;
    
    private String street;
    
    private String locality;
    
    private String surname;
    
    @JsonProperty("given_name")
    private String givenName;
    
    private String inn;
    
    private String kpp;
    
    private String ogrn;
    
    private String snils;
    
    private String ogrnip;
    
}
