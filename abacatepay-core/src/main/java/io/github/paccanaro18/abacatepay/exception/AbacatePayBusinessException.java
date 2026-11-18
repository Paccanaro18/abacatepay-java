package io.github.paccanaro18.abacatepay.exception;

public class AbacatePayBusinessException extends AbacatePayException {
    public AbacatePayBusinessException(String message) {
        super(400, message);
    }
}