package ru.gostmaster.verification.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * Ошибка, которая возникает, когда  даже не успели приступить к проверке подписи.
 * 
 * @author maksimgurin 
 */
@Data
public class VerificationError {
    @JsonProperty("error_date")
    private Date errorDate;
    @JsonProperty("error_description")
    private String errorDescription;
}
