package io.github.paccanaro18.abacatepay.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import io.github.paccanaro18.abacatepay.exception.WebhookParseException;

public record WebhookEvent(String id, String event, int apiVersion, boolean devMode, JsonNode data) {

    public WebhookEventType type() {
        return WebhookEventType.fromValue(event);
    }

    public CheckoutEventData checkoutData() {
        if (!type().isCheckout()) {
            throw new IllegalStateException("O evento '" + event + "' não é um evento de checkout");
        }
        if (data == null || data.isNull()) {
            throw new WebhookParseException("O evento '" + event + "' veio sem o campo 'data'", null);
        }
        try {
            return AbacatePayJson.mapper().treeToValue(data, CheckoutEventData.class);
        } catch (JsonProcessingException e) {
            throw new WebhookParseException("Payload de checkout inválido: " + e.getOriginalMessage(), e);
        }
    }
}