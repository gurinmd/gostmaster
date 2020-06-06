package ru.gostmaster.model;

import lombok.Data;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.gostmaster.data.cert.Certificate;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Реализация сущности сертификата для хранения в MongoDB.
 *
 * @author maksimgurin
 */
@Data
@Document(collection = "certs")
@ToString
public class MongoCertificateData implements Certificate {
    public static final String COLLECTION = MongoCertificateData.class.getAnnotation(Document.class).collection();
    public static final String F_TRUSTED = "trusted";
    public static final String F_SUBJECT_KEY = "subjectKey";
    public static final String F_SN = "sn";
    public static final String F_SUBJECT_EXPRESSION = "$" + F_SUBJECT_KEY;
    public static final String F_ISSUER_KEY = "issuerKey";
    public static final String F_CHAIN = "chain";
    
    @Id
    private ObjectId id;
    @Indexed
    private String issuerKey;
    @Indexed
    private String subjectKey;
    private BigInteger sn;
    private Date startDate;
    private Date endDate;
    private String pemData;
    private boolean trusted;
    private List<String> crlUrls;
    
    private List<MongoCertificateData> chain;
}
