package ru.gostmaster.verification.data;

import lombok.Data;

/**
 * Область юридического принимения сертификата. Класс УКЭП.
 * 
 * @author maksimgurin 
 */
@Data
public class CertificatePolicy {
    private String oid;
    private String name;
}
