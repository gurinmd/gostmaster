package ru.gostmaster.dictionary;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Имена политик сертификатов.
 * 
 * @author maksimgurin 
 */
public final class CertificatePoliciesName {
    
    public static final String KS1_OID = "1.2.643.100.113.1";
    public static final String KS2_OID = "1.2.643.100.113.2";
    public static final String KS3_OID = "1.2.643.100.113.3";

    public static final String KV1_OID = "1.2.643.100.113.4";
    public static final String KV2_OID = "1.2.643.100.113.5";

    public static final String KA1_OID = "1.2.643.100.113.6";
    
    private static final Map<String, String> CERTIFICATE_POLICIES_NAMES = new ImmutableMap.Builder()
        .put("1.2.643.100.113.1", "класс средства ЭП КС1")
        .put("1.2.643.100.113.2", "класс средства ЭП КС2")
        .put("1.2.643.100.113.3", "класс средства ЭП КС3")
        .put("1.2.643.100.113.4", "класс средства ЭП КВ1")
        .put("1.2.643.100.113.5", "класс средства ЭП КВ2")
        .put("1.2.643.100.113.6", "класс средства ЭП КА1")
        .build();
    
    private CertificatePoliciesName() { }

    /**
     * Получить имя по OID.
     * @param oid oid
     * @return имя или  oid, если oid не найден
     */
    public static String getPolicyName(String oid) {
        return CERTIFICATE_POLICIES_NAMES.getOrDefault(oid, oid);
    }
} 
