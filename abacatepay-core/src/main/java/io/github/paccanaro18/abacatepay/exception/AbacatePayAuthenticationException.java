package io.github.paccanaro18.abacatepay.exception;

public class AbacatePayAuthenticationException extends AbacatePayException {
    public AbacatePayAuthenticationException(String message) {
        super(401, message);
    }
}
