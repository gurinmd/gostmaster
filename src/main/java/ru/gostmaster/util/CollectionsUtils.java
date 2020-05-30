package ru.gostmaster.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Методы для работы с коллекциями.
 * 
 * @author maksimgurin 
 */
public final class CollectionsUtils {

    private CollectionsUtils() { }
        
    /**
     * Меняем список так, чтобы остались только уникальные элементы.
     * @param sourceList исходный список
     * @param extractor функция, которая по элементу списка отдает поле, которые должны быть уникальными
     * @param <T> тип элемента списка
     * @param <K> тип ключа, по которому проверяется уникальность
     * @return список из уникальных значений
     */
    public static <T, K> List<T> makeUniqueList(List<T> sourceList, Function<T, K> extractor) {
        Map<K, T> map = new HashMap<>();
        Optional.ofNullable(sourceList)
            .orElse(Collections.emptyList())
            .forEach(t -> map.put(extractor.apply(t), t));
        List<T> res = new ArrayList<>(map.values());
        return res;
    }
}
