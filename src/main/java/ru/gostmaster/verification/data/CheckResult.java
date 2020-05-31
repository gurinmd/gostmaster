package ru.gostmaster.verification.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * DTO для представления определенного шага проверки подписи.
 * 
 * @author maksimgurin 
 */
@Data
public class CheckResult {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("result_description")
    private String resultDescription;
    
    @JsonProperty("created_at")
    private Date createdAt;
}
