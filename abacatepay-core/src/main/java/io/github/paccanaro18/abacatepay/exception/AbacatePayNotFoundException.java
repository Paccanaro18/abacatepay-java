package io.github.paccanaro18.abacatepay.exception;

public class AbacatePayNotFoundException extends AbacatePayException {
    public AbacatePayNotFoundException(String message) {
        super(404, message);
    }
}
