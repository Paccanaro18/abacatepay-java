package io.github.paccanaro18.abacatepay.webhook;

import io.github.paccanaro18.abacatepay.AbacatePayJson;
import io.github.paccanaro18.abacatepay.exception.WebhookParseException;

import java.io.IOException;

public final class WebhookParser {

    private WebhookParser() {
    }

    public static WebhookEvent parse(byte[] rawBody) {
        if (rawBody == null || rawBody.length == 0) {
            throw new WebhookParseException("Corpo do webhook vazio", null);
        }

        WebhookEvent event;
        try {
            event = AbacatePayJson.mapper().readValue(rawBody, WebhookEvent.class);
        } catch (IOException e) {
            throw new WebhookParseException("Corpo do webhook não é um JSON válido", e);
        }

        if (event.event() == null || event.event().isBlank()) {
            throw new WebhookParseException("Webhook sem o campo 'event'", null);
        }
        return event;
    }
}