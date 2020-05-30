package ru.gostmaster.common.messages;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс с сообщениями, которые отправляются в ответе пользователю.
 * 
 * @author maksimgurin 
 */
public final class Messages {
    
    public static final String CORRUPTED_DATA = "CORRUPTED_DATA";
    public static final String VALID_SIGNED_CONTENT = "VALID_SIGNED_CONTENT";
    public static final String INVALID_SIGNED_CONTENT = "INVALID_SIGNED_CONTENT";
    public static final String CERTIFICATE_EXPIRED = "CERTIFICATE_EXPIRED";
    public static final String CERTIFICATE_NOT_YET_VALID = "CERTIFICATE_NOT_YET_VALID";
    public static final String CERTIFICATE_REVOKED = "CERTIFICATE_REVOKED";
    public static final String CERTIFICATE_VALID = "CERTIFICATE_VALID";
    
    private static Map<String, String> messages = new HashMap();
    
    static {
        messages.put(CORRUPTED_DATA, "Не удалось проивести проверку ЭЦП: ошибка инициализации данных. Возможно, данные  повреждены");
        messages.put(INVALID_SIGNED_CONTENT, "Подпись не соотвествует подписанному содержимому");
        messages.put(VALID_SIGNED_CONTENT, "Подпись соответствует подписанному содержимому");
        messages.put(CERTIFICATE_EXPIRED, "Срок действия сертификата истек");
        messages.put(CERTIFICATE_NOT_YET_VALID, "Срок действия сертификата не наступил");
        messages.put(CERTIFICATE_REVOKED, "Сертификат был отозван");
        messages.put(CERTIFICATE_VALID, "Действительность сертификата подтверждена");
    }
    
    private Messages() { }

    /**
     * Получить сообщение по ключу.
     * @param key ключ
     * @return сообщение или ключ, если сообщение по ключу не найдено
     */
    public static String getMessage(String key) {
        return messages.getOrDefault(key, key);
    }
}
