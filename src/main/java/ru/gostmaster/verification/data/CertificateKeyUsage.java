package ru.gostmaster.verification.data;

import lombok.Data;

/**
 * DTO для описания использования сертификата.
 * 
 * @author maksimgurin 
 */
@Data
public class CertificateKeyUsage {
    private String oid;
    private String name;
}
