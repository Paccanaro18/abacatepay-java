package io.github.paccanaro18.abacatepay.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import io.github.paccanaro18.abacatepay.ApiResponse;
import io.github.paccanaro18.abacatepay.Page;
import io.github.paccanaro18.abacatepay.PagedApiResponse;
import io.github.paccanaro18.abacatepay.exception.AbacatePayConnectionException;
import io.github.paccanaro18.abacatepay.exception.AbacatePayException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public final class AbacatePayHttpClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient http;
    private final String baseUrl;
    private final String apiKey;
    private final Duration timeout;

    public AbacatePayHttpClient(String apiKey, String baseUrl) {
        this(apiKey, baseUrl, DEFAULT_TIMEOUT);
    }

    public AbacatePayHttpClient(String apiKey, String baseUrl, Duration timeout) {
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl").replaceAll("/+$", "");
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        this.http = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    public <T> T get(String path, TypeReference<ApiResponse<T>> type) {
        return send(HttpRequest.newBuilder(URI.create(baseUrl + path)).GET(), type);
    }

    public <T> Page<T> getPage(String path, TypeReference<PagedApiResponse<T>> type) {
        HttpResponse<String> response = execute(HttpRequest.newBuilder(URI.create(baseUrl + path)).GET());
        return extractPage(response.body(), response.statusCode(), type);
    }

    public <T> T post(String path, Object body, TypeReference<ApiResponse<T>> type) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path));
        if (body == null) {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(body)));
        }
        return send(builder, type);
    }

    private <T> T send(HttpRequest.Builder builder, TypeReference<ApiResponse<T>> type) {
        HttpResponse<String> response = execute(builder);
        return extractData(response.body(), response.statusCode(), type);
    }

    private HttpResponse<String> execute(HttpRequest.Builder builder) {
        HttpRequest request = builder
                .timeout(timeout)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new AbacatePayConnectionException("Falha ao chamar o AbacatePay: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AbacatePayConnectionException("Chamada ao AbacatePay interrompida", e);
        }

        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw ErrorMapper.map(status, response.body(), retryAfterSeconds(response));
        }
        return response;
    }

    private <T> T extractData(String body, int status, TypeReference<ApiResponse<T>> type) {
        ApiResponse<T> envelope;
        try {
            envelope = AbacatePayJson.mapper().readValue(body, type);
        } catch (JsonProcessingException e) {
            throw new AbacatePayException(status, "Resposta do AbacatePay não é um JSON válido", e);
        }

        if (!envelope.success()) {
            String message = envelope.error() != null ? envelope.error() : "Resposta sem sucesso";
            throw new AbacatePayException(status, message);
        }
        return envelope.data();
    }

    private <T> Page<T> extractPage(String body, int status, TypeReference<PagedApiResponse<T>> type) {
        PagedApiResponse<T> envelope;
        try {
            envelope = AbacatePayJson.mapper().readValue(body, type);
        } catch (JsonProcessingException e) {
            throw new AbacatePayException(status, "Resposta do AbacatePay não é um JSON válido", e);
        }

        if (!envelope.success()) {
            String message = envelope.error() != null ? envelope.error() : "Resposta sem sucesso";
            throw new AbacatePayException(status, message);
        }

        List<T> items = envelope.data() != null ? envelope.data() : List.of();
        PagedApiResponse.Pagination pagination = envelope.pagination();
        if (pagination == null) {
            return new Page<>(items, false, null, null);
        }
        return new Page<>(items, pagination.hasMore(), pagination.next(), pagination.before());
    }

    private static String toJson(Object body) {
        try {
            return AbacatePayJson.mapper().writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Não foi possível converter o corpo da requisição em JSON", e);
        }
    }

    private static Long retryAfterSeconds(HttpResponse<?> response) {
        return response.headers().firstValue("RateLimit-Reset").map(valor -> {
            try {
                return Long.parseLong(valor.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }).orElse(null);
    }
}
