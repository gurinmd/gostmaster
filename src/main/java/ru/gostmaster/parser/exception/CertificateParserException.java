package ru.gostmaster.parser.exception;

/**
 * Исключение в случае ошибки парсинга сертификата.
 * 
 * @author maksimgurin 
 */
public class CertificateParserException extends RuntimeException {
    public CertificateParserException() {
    }

    public CertificateParserException(String message) {
        super(message);
    }

    public CertificateParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public CertificateParserException(Throwable cause) {
        super(cause);
    }

    public CertificateParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
