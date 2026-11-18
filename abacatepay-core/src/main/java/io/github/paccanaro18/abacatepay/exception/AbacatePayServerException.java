package io.github.paccanaro18.abacatepay.exception;

public class AbacatePayServerException extends AbacatePayException {
    public AbacatePayServerException(int statusCode, String message) {
        super(statusCode, message);
    }
}