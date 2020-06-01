package ru.gostmaster.data.crl;

import java.util.Date;

/**
 * Интерфейс для сущности "ссылка  на crl". Хранит данные, последнего скачивания и дату, когда надо перекачать.
 *
 * @author maksimgurin
 */
public interface CrlUrl {

    /**
     * Получить ссылку для скачивания CRL.
     *
     * @return ссылка
     */
    String getUrl();

    /**
     * Получить дату последнего скачивания.
     *
     * @return дата
     */
    Date getCurrentUpdate();

    /**
     * Получить дату, когда надо обновить в следующий раз.
     *
     * @return дата
     */
    Date getNextUpdate();

}
