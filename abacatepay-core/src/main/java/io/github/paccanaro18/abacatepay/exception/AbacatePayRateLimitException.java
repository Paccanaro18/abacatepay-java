package io.github.paccanaro18.abacatepay.exception;

public class AbacatePayRateLimitException extends AbacatePayException {

    private final Long retryAfterSeconds; // null se o header não vier

    public AbacatePayRateLimitException(String message, Long retryAfterSeconds) {
        super(429, message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public Long retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
