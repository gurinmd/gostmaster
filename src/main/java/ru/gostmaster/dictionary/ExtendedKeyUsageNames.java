package ru.gostmaster.dictionary;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Класс для получения имен использования сертификата.
 * 
 * @author maksimgurin 
 */
public final class ExtendedKeyUsageNames {
    
    private static final Map<String, String> EXT_KEY_USAGE_NAMES = new ImmutableMap.Builder<String, String>()
        .put("1.3.6.1.5.5.7.3.2", "Проверка подлинности клиента")
        .put("1.3.6.1.5.5.7.3.4", "Защищенная электронная почта")
        .put("1.2.643.2.2.34.6", "Пользователь Центра Регистрации")
        .put("1.2.643.2.2.34.26", "Пользователь службы актуальных статусов")
        .put("1.2.643.2.2.34.25", "Пользователь службы штампов времени")
        .put("1.2.643.5.3.48.1", "Пользователь управления финансов Липецкой области")
        .put("1.2.643.5.3.40.1", "Пользователь управления финансов Калужской области")
        .put("1.2.643.2.23.3", "Пользователь управления финансов Самарской области")
        .put("1.2.643.3.41.1.3.4", "Пользователь управления финансов Курганской области")
        .put("1.2.643.7.2.21.1.2", "Размещение сведений в сводном реестре")
        .put("1.2.643.3.89.24", "Пользователь zapret-info.gov.ru")

        .put("1.2.643.6.3.1.1", "Использование на ЭТП для аукционов")
        .put("1.2.643.6.3.1.3.1", "Участник размещения заказа")
        .put("1.2.643.6.3.1.2.1", "Юридическое лицо")
        .put("1.2.643.6.3.1.2.2", "Физическое лицо")
        .put("1.2.643.6.3.1.2.3", "Индивидуальный предприниматель")
        .put("1.2.643.5.5.66.1", "ЭТС \"МТП\"")
        .put("1.2.643.3.8.100.1.42", "Использование КЭП на ЭТП www.utpl.ru")

        .put("1.2.643.6.3.1.4.3", "Специалист с правом подписи контакта")
        .put("1.2.643.6.3.1.4.1", "Администратор организации")
        .put("1.2.643.6.3.1.4.2", "Уполномоченный специалист")

        .put("1.2.643.6.17", "Газпромбанк")
        .put("1.2.643.3.8.100.1.19", "UralBidin")

        .put("1.2.643.3.61.502710.1.6.3.4.1.1", "ЭТП - Заказчик: Администратор организации")
        .put("1.2.643.3.61.502710.1.6.3.4.1.2", "ЭТП - Заказчик: Уполномоченный специалист")
        .put("1.2.643.3.61.502710.1.6.3.4.1.3", "ЭТП - Заказчик: Специалист с правом подписи контракта")
        .put("1.2.643.3.61.502710.1.6.3.4.1.4", "ЭТП - Заказчик: Специалист с правом направления шаблона контракта")
        .put("1.2.643.3.61.502710.1.6.3.4.2", "ЭТП - Уполномоченная организация: базовый OID")
        .put("1.2.643.3.61.502710.1.6.3.4.2.1", "ЭТП - Уполномоченная организация: администратор")
        .put("1.2.643.3.61.502710.1.6.3.4.2.2", "ЭТП - Уполномоченная организация: уполномоченный специалист")
        .put("1.2.643.3.61.502710.1.6.3.4.2.3", "ЭТП - Уполномоченная организация: специалист с правом " +
            "направления шаблона контракта")
        .put("1.2.643.3.61.502710.1.6.3.4.2.4", "ЭТП - Уполномоченная организация: должностное лицо с правом" +
            " подписи контракта")
        .put("1.2.643.3.61.502710.1.6.3.4.2.5", "ЭТП - Уполномоченная организация: специалист с правом " +
            "согласования размещения заказа")

        .put("1.2.643.7.2.50.1.2", "Пользователь ЕСИА")

        .put("1.2.643.3.215.4", "ФТС")
        .put("1.2.643.3.215.6", "ФТС")
        .put("1.2.643.3.215.7", "ФТС")
        .put("1.2.643.3.215.8", "ФТС")
        .put("1.2.643.3.215.9", "ФТС")
        .put("1.2.643.3.215.11", "ФТС")
        .put("1.2.643.3.215.12", "ФТС")
        .put("1.2.643.3.215.13", "ФТС")
        
        .put("1.2.643.5.1.31.1", "ФСФР")
        .put("1.2.643.3.89.21", "Росимущество")
        .put("1.2.643.3.7.3.3", "ФСТ")

        .put("1.2.643.5.1.24.2.1.3.1 ", "Росреестр - Кадастровый инженер")
        .put("1.2.643.5.1.24.2.30", "Росреестр - Запрос из ЕГРП для ЮЛ")
        .put("1.2.643.5.1.24.2.1.3", "Росреестр - Запрос из ЕГРП для ФЛ")
        .put("1.2.643.5.1.24.2.27", "Росреестр - Арбитражный управляющий")
        .put("1.2.643.5.1.24.2.8", "Росреестр - Судья")
        .put("1.2.643.5.1.24.2.49", "Росреестр - УЛ представителя органа власти")
        .put("1.2.643.5.1.24.3.3.10", "Росреестр - Кредитная организация")
        .put("1.2.643.5.1.24.2.48", "Росреестр - Сведения о зданиях, сооружениях, незавершенном строительстве")
        .put("1.2.643.5.1.24.2.15", "Росреестр - Правоохранительный орган")
        .put("1.2.643.5.1.24.2.26", "Росреестр - Нотариус")
        .put("1.2.643.5.1.24.2.14", "Росреестр - Пристав-исполнитель")
        
        .put("1.2.643.100.2.1", "СМЭВ + Росреестр - УЛ представителя органа власти")
        .put("1.2.643.5.1.24.2.6", "СМЭВ + Росреестр - Орган государственной власти субъекта РФ")
        .put("1.2.643.5.1.24.2.20", "СМЭВ + Росреестр - Федеральный орган исполнительной власти")
        .put("1.2.643.5.1.24.2.43", "СМЭВ + Росреестр - Территориальный орган федерального органа" +
            " исполнительной власти")
        .put("1.2.643.5.1.24.2.19", "СМЭВ + Росреестр - Орган местного самоуправления")
        .put("1.2.643.5.1.24.2.5", "СМЭВ + Росреестр - Орган местного самоуправления по учету муниципального имущества")
        .put("1.2.643.5.1.24.2.44", "СМЭВ + Росреестр - Руководитель или иное уполномоченное лицо орган" +
            " исполнительной власти по учету муниципального имущества")
        .put("1.2.643.3.58.2.1.6", "СМЭВ + Росреестр - Идентификатор владельца подписи")
        .put("1.2.643.5.1.24.2.53", "СМЭВ + Росреестр - Подведомственная организация органа государственной" +
            " власти субъекта РФ")
        .put("1.2.643.5.1.24.2.52", "СМЭВ + Росреестр - Территориального органа государственного внебюджетного фонда")
        .put("1.2.643.5.1.24.2.51", "СМЭВ + Росреестр - Государственный внебюджетный фонд")
        
        .put("1.2.643.100.2.2", "СМЭВ - Орган власти")
        .put("1.2.643.5.1.28.2", "Система декларирования ФСРАР")
        .put("1.2.643.2.64.1.1.1", "ЕФРСДЮЛ")
        
        .put("1.2.643.3.58.2.1.3", "Инженер УЦ")

        .build();

    private ExtendedKeyUsageNames() { }

    /**
     * Получить имя, для чего может использоваться сертификат.
     * @param algOid oid
     * @return тестовое описание
     */
    public static String getUsageName(String algOid) {
        return EXT_KEY_USAGE_NAMES.getOrDefault(algOid, algOid);
    }
}
