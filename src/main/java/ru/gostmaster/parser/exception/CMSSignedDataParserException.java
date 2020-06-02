package ru.gostmaster.parser.exception;

/**
 * Исключение, когда не можем распарсить подпись.
 * 
 * @author maksimgurin 
 */
public class CMSSignedDataParserException extends RuntimeException {
    public CMSSignedDataParserException() {
    }

    public CMSSignedDataParserException(String message) {
        super(message);
    }

    public CMSSignedDataParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public CMSSignedDataParserException(Throwable cause) {
        super(cause);
    }

    public CMSSignedDataParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
