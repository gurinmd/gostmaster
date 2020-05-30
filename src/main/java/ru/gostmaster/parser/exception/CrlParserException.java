package ru.gostmaster.parser.exception;

/**
 * Исключене при ошибке парсинга CRL.
 * 
 * @author maksimgurin 
 */
public class CrlParserException extends RuntimeException {
    public CrlParserException() {
    }

    public CrlParserException(String message) {
        super(message);
    }

    public CrlParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public CrlParserException(Throwable cause) {
        super(cause);
    }

    public CrlParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
