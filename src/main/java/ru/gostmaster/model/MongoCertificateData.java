package ru.gostmaster.model;

import lombok.Data;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.gostmaster.common.data.cert.Certificate;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "certs")
@ToString
public class MongoCertificateData implements Certificate {
    public static final String COLLECTION = MongoCertificateData.class.getAnnotation(Document.class).collection();
    public static final String F_TRUSTED = "trusted";
    public static final String F_SUBJECT = "subject";
    public static final String F_SN = "sn";
    public static final String F_SUBJECT_EXPRESSION = "$"+F_SUBJECT;
    public static final String F_ISSUER = "issuer";
    public static final String F_CHAIN = "chain";
    
    @Id
    private ObjectId id;
    private String issuer;
    private String subject;
    private BigInteger sn;
    private Date startDate;
    private Date endDate;
    private String certificateEncodedData;
    private boolean trusted;
    private List<String> crlUrls;
    
    private List<MongoCertificateData> chain;
}
