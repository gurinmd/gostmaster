package ru.gostmaster.common.data.verification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CertificateSubject {
    private String organization;
    @JsonProperty("organization_unit")
    private String organizationUnit;
    private String name;
    private String inn;
    private String ogrn;
    private String location;
    private String email;
}
