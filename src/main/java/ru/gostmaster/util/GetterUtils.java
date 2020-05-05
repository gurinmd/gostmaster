package ru.gostmaster.util;

import java.util.function.Supplier;

public class GetterUtils {
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
