package ru.gostmaster.util;

import java.time.Instant;
import java.util.Date;

/**
 * Класс для работы с датами.
 * 
 * @author maksimgurin 
 */
public final class DateUtils {
    private DateUtils() { }

    /**
     * Делаем из даты Instant.
     * @param date дата
     * @return instant
     */
    public static Instant fromDate(Date date) {
        if (date != null) {
            return Instant.ofEpochMilli(date.getTime());
        } else {
            return null;
        }
    }
}
