package io.github.paccanaro18.abacatepay.exception;


public class WebhookParseException extends AbacatePayException {
    public WebhookParseException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
