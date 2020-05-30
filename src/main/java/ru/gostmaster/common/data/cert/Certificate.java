package ru.gostmaster.common.data.cert;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Интерфейс представления сертификата.
 * 
 * @author maksimgurin 
 */
public interface Certificate {

    /**
     * Идентификатор издателя сертификата.
     * @return идентификатор
     */
    String getIssuerKey();

    /**
     * Идентификатор владельца сертификата.
     * @return идентификатор
     */
    String getSubjectKey();

    /**
     * Серийный номер сертификата.
     * @return номер
     */
    BigInteger getSn();

    /**
     * Дата начала действия сертификата.
     * @return дата
     */
    Date getStartDate();

    /**
     * Дата окончания действия сертификата.
     * @return дата
     */
    Date getEndDate();

    /**
     * Данные сертификата в формате PEM.
     * @return pem
     */
    String getPemData();

    /**
     * Флаг, является ли сертификат доверенным.
     * @return флаг
     */
    boolean isTrusted();

    /**
     * Списки CRL для этого сертификата.
     * @return список url
     */
    List<String> getCrlUrls();
    
    /**
     * Флаг, является ли сертификат доверенным.
     * @param trusted флаг
     */
    void setTrusted(boolean trusted);
}
