package ru.gostmaster.data.crl;

import java.util.Date;

/**
 * Общий интерфейс представления CRL.
 * 
 * @author maksimgurin 
 */
public interface Crl {

    /**
     * Идентификатор издателя.
     * @return идентификатор издателя.
     */
    String getIssuerKey();

    /**
     * Данные сертификата в формате PEM.
     * @return PEM представление
     */
    String getPemData();

    /**
     * Дата следующего обновления CRL.
     * @return дата
     */
    Date getNextUpdate();

    /**
     * Дата текущего обновления CRL.
     * @return дата
     */
    Date getThisUpdate();

    /**
     * Ссылка, откуда был скачан этот список.
     * @return ссылка
     */
    String getDownloadedFrom();
}
