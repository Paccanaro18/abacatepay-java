package io.github.paccanaro18.abacatepay.exception;

public class WebhookVerificationException extends AbacatePayException {
    public WebhookVerificationException(String message) {
        super(401, message);
    }
}