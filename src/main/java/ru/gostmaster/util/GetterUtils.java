package ru.gostmaster.util;

import java.util.function.Supplier;

/**
 * Утилитный класс для вызова Changed Getter без опасения поймат NPE.
 * 
 * @author maksimgurin 
 */
public final class GetterUtils {

    private GetterUtils() { }
    
    /**
     * Метод для безопасной обертки Getter oв.
     * Пример: {@code String s = GetterUtils.get(() -> user.getAddress().getStreet().getName(), "def street name")}
     * @param supplier лямба выражение,в которое оборачивается вызов геттера
     * @param defaultValue значение по умолчанию. отдается, когда при выполнении лямба выражения произошла ошибка
     * @param <T> тип 
     * @return значение из лямбы,  если не было ошибок, значение по умолчанию в противном случае.
     */
    public static <T> T get(Supplier<T> supplier, T defaultValue) {
        T res;
        try {
            res = supplier.get();
        } catch (Exception ex) {
            res = defaultValue;
        }
        return res;
    }
}
