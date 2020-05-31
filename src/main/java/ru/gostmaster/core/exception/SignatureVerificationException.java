package ru.gostmaster.core.exception;

/**
 * Исключение при проверке подписи.
 * 
 * @author maksimgurin 
 */
public class SignatureVerificationException extends RuntimeException {
    public SignatureVerificationException() {
    }

    public SignatureVerificationException(String message) {
        super(message);
    }

    public SignatureVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignatureVerificationException(Throwable cause) {
        super(cause);
    }

    public SignatureVerificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
