package ru.gostmaster.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.gostmaster.data.crl.Crl;

import java.util.Date;

/**
 * Реализация сущности списка отозванных сертификатов (CRL) для хранения в MongoDB.
 * 
 * @author maksimgurin 
 */
@Document(collection = "crl")
@Data
@ToString
public class MongoCrlData implements Crl {
    public static final String F_ISSUER_KEY = "issuerKey";
    public static final String F_DONWLOADED_FROM = "downloadedFrom";
    @Indexed
    private String issuerKey;
    private String issuer;
    private String pemData;
    private Date nextUpdate;
    private Date thisUpdate;
    @Indexed
    private String downloadedFrom;
}
