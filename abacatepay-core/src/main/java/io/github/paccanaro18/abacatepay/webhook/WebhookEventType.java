package io.github.paccanaro18.abacatepay.webhook;

public enum WebhookEventType {
    CHECKOUT_COMPLETED("checkout.completed"),
    CHECKOUT_REFUNDED("checkout.refunded"),
    CHECKOUT_DISPUTED("checkout.disputed"),
    UNKNOWN(null);

    private final String value;

    WebhookEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public boolean isCheckout() {
        return this == CHECKOUT_COMPLETED || this == CHECKOUT_REFUNDED || this == CHECKOUT_DISPUTED;
    }

    public static WebhookEventType fromValue(String value) {
        for (WebhookEventType type : values()) {
            if (type.value != null && type.value.equals(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}