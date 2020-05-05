package ru.gostmaster.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.gostmaster.common.data.crl.Crl;

import java.util.Date;

@Document(collection = "crl")
@Data
@ToString
public class MongoCrlData implements Crl {
    public static final String F_CA_SUBJECT = "caSubject";
    public static final String F_DONWLOADED_FROM = "downloadedFrom";
    @Indexed
    private String caSubject;
    private String crlEncodedData;
    private Date nextUpdate;
    private Date thisUpdate;
    @Indexed
    private String downloadedFrom;
}
