package io.github.paccanaro18.abacatepay.exception;

public class AbacatePayValidationException extends AbacatePayException {
    public AbacatePayValidationException(String message) {
        super(422, message);
    }
}
