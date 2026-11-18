package io.github.paccanaro18.abacatepay.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import io.github.paccanaro18.abacatepay.exception.*;

final class ErrorMapper {

    private ErrorMapper() {
    }

    static AbacatePayException map(int status, String body, Long retryAfterSeconds) {
        String message = extractMessage(body, status);

        return switch (status) {
            case 400 -> new AbacatePayBusinessException(message);
            case 401 -> new AbacatePayAuthenticationException(message);
            case 404 -> new AbacatePayNotFoundException(message);
            case 422 -> new AbacatePayValidationException(message);
            case 429 -> new AbacatePayRateLimitException(message, retryAfterSeconds);
            default -> status >= 500
                    ? new AbacatePayServerException(status, message)
                    : new AbacatePayException(status, message);
        };
    }

    private static String extractMessage(String body, int status) {
        String fallback = "HTTP " + status;

        if (body == null || body.isBlank()) {
            return fallback;
        }

        try {
            JsonNode error = AbacatePayJson.mapper().readTree(body).get("error");
            if (error != null && !error.isNull()) {
                return error.asText();
            }
        } catch (JsonProcessingException e) {
            // corpo não é JSON (ex.: HTML de um proxy): cai na mensagem padrão
        }
        return fallback;
    }
}