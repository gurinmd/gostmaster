package ru.gostmaster.model;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.gostmaster.data.crl.CrlUrl;

import java.util.Date;

/**
 * Класс хранения объекта ссылки на CRL в MongoDB.
 *
 * @author maksimgurin
 */
@Document(collection = "crl_urls")
@Data
public class MongoCrlUrlData implements CrlUrl {

    public static final String F_URL = "url";
    public static final String F_CURRENT_UPDATE = "currentUpdate";
    public static final String F_NEXT_UPDATE = "nextUpdate";

    @Indexed(unique = true)
    private String url;

    private Date currentUpdate;

    private Date nextUpdate;
}
