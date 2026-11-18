package io.github.paccanaro18.abacatepay.exception;

public class AbacatePayException extends RuntimeException {

    private final int statusCode;

    public AbacatePayException(int statusCode, String message) {
        this(statusCode, message, null);
    }

    public AbacatePayException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}