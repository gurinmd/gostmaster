package ru.gostmaster.common.data.cert;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public interface Certificate {
    
    String getIssuer();
    String getSubject();
    BigInteger getSn();
    Date getStartDate();
    Date getEndDate();
    String getCertificateEncodedData();
    boolean isTrusted();
    List<String> getCrlUrls();
}
