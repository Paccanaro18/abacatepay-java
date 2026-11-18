package io.github.paccanaro18.abacatepay.spring;

import io.github.paccanaro18.abacatepay.AbacatePay;
import io.github.paccanaro18.abacatepay.webhook.AbacatePayWebhooks;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "abacatepay")
public record AbacatePayProperties(
        String apiKey,
        @DefaultValue(AbacatePay.DEFAULT_BASE_URL) String baseUrl,
        @DefaultValue Webhook webhook) {

    public record Webhook(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("/abacatepay/webhook") String path,
            String secret,
            @DefaultValue(AbacatePayWebhooks.PUBLIC_KEY) String publicKey) {
    }
}