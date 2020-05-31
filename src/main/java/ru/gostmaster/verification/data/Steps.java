package ru.gostmaster.verification.data;

/**
 * Утилитный класс описания шагов проверки. Описываются коды и описания шагов.
 * 
 * @author maksimgurin 
 */
public final class Steps {
    
    public static final String CHECK_SIGN_CONTENT = "checkSignedData";
    public static final String CHECK_SIGN_CONTENT_DESCRIPTION = "Проверка соответствия содержимого документа подписи" +
        " документа";
    
    public static final String CHECK_KEY_USAGE = "checkKeyUsage";
    public static final String CHECK_KEY_USAGE_DESCRIPTION = "Проверка применимости ключа сертификата" +
        " для подписания документа";
    
    public static final String CHECK_CERTIFICATE_CHAIN = "checkCertChain";
    public static final String CHECK_CERTIFICATE_CHAIN_DESCRIPTION = "Проверка существования цепочки сертификатов" +
        " от доверенного до сертификата подписи";
    
    public static final String CHECK_CERTIFICATE_CHAIN_WITH_CRL = "checkCertChainWithCRL";
    public static final String CHECK_CERTIFICATE_CHAIN_WITH_CRL_DESCRIPTION = "Проверка существования цепочки " +
        "сертификатов от доверенного до сертификата подписи c учетом списка отозванных сертификатов";
    
    private Steps() { }
}
