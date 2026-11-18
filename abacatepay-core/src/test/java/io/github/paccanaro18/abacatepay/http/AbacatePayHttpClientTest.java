package io.github.paccanaro18.abacatepay.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpServer;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import io.github.paccanaro18.abacatepay.ApiResponse;
import io.github.paccanaro18.abacatepay.exception.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AbacatePayHttpClientTest {

    record Bill(String id, String status) {}

    private static final TypeReference<ApiResponse<Bill>> BILL = new TypeReference<>() {};

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> authorizationRecebido = new AtomicReference<>();
    private final AtomicReference<String> corpoRecebido = new AtomicReference<>();

    @BeforeEach
    void subirServidor() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void pararServidor() {
        server.stop(0);
    }

    private void responder(String path, int status, String corpo, Map<String, String> headers) {
        server.createContext(path, exchange -> {
            authorizationRecebido.set(exchange.getRequestHeaders().getFirst("Authorization"));
            corpoRecebido.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            headers.forEach((nome, valor) -> exchange.getResponseHeaders().add(nome, valor));
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream saida = exchange.getResponseBody()) {
                saida.write(bytes);
            }
        });
    }

    private AbacatePayHttpClient cliente() {
        return new AbacatePayHttpClient("abc_dev_123", baseUrl);
    }

    @Test
    void getDevolveDataEEnviaBearer() {
        responder("/checkouts/get", 200,
                "{\"success\":true,\"error\":null,\"data\":{\"id\":\"bill_1\",\"status\":\"PENDING\"}}",
                Map.of());

        Bill bill = cliente().get("/checkouts/get", BILL);

        assertEquals("bill_1", bill.id());
        assertEquals("PENDING", bill.status());
        assertEquals("Bearer abc_dev_123", authorizationRecebido.get());
    }

    @Test
    void postEnviaCorpoJson() throws Exception {
        responder("/checkouts/create", 200,
                "{\"success\":true,\"error\":null,\"data\":{\"id\":\"bill_2\",\"status\":\"PENDING\"}}",
                Map.of());

        Map<String, Object> corpo = Map.of("items", List.of(Map.of("id", "prod_1", "quantity", 1)));
        Bill bill = cliente().post("/checkouts/create", corpo, BILL);

        assertEquals("bill_2", bill.id());
        String itemEnviado = AbacatePayJson.mapper()
                .readTree(corpoRecebido.get()).get("items").get(0).get("id").asText();
        assertEquals("prod_1", itemEnviado);
    }

    @Test
    void status401VirouExcecaoDeAutenticacao() {
        responder("/checkouts/get", 401, "{\"error\":\"Invalid or inactive API key\"}", Map.of());

        AbacatePayAuthenticationException ex = assertThrows(
                AbacatePayAuthenticationException.class,
                () -> cliente().get("/checkouts/get", BILL));

        assertEquals("Invalid or inactive API key", ex.getMessage());
    }

    @Test
    void status429GuardaTempoDoHeaderRateLimitReset() {
        responder("/checkouts/get", 429, "{\"error\":\"Too many requests\"}",
                Map.of("RateLimit-Reset", "3"));

        AbacatePayRateLimitException ex = assertThrows(
                AbacatePayRateLimitException.class,
                () -> cliente().get("/checkouts/get", BILL));

        assertEquals(3L, ex.retryAfterSeconds());
    }

    @Test
    void respostaDeSucessoQueNaoEJsonViraExcecao() {
        responder("/checkouts/get", 200, "<html>oops</html>", Map.of());

        assertThrows(AbacatePayException.class, () -> cliente().get("/checkouts/get", BILL));
    }

    @Test
    void servidorForaDoArViraExcecaoDeConexao() {
        server.stop(0);

        AbacatePayConnectionException ex = assertThrows(
                AbacatePayConnectionException.class,
                () -> cliente().get("/checkouts/get", BILL));

        assertNotNull(ex.getCause());
        assertEquals(0, ex.statusCode());
    }
}