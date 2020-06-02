package ru.gostmaster.verification.exception;

/**
 * Исключение при ошибке загрузки подписи.
 * 
 * @author maksimgurin 
 */
public class SignatureUploadException extends RuntimeException {
    public SignatureUploadException() {
    }

    public SignatureUploadException(String message) {
        super(message);
    }

    public SignatureUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignatureUploadException(Throwable cause) {
        super(cause);
    }

    public SignatureUploadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
