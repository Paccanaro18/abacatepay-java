package io.github.paccanaro18.abacatepay;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public final class FakeApi implements AutoCloseable {

    private final HttpServer server;
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> body = new AtomicReference<>();
    private final AtomicReference<String> uri = new AtomicReference<>();
    private final AtomicReference<String> method = new AtomicReference<>();

    public FakeApi() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        server.start();
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    public void respond(String path, int status, String responseBody) {
        server.createContext(path, exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            uri.set(exchange.getRequestURI().toString());
            method.set(exchange.getRequestMethod());
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
    }

    public String lastAuthorization() {
        return authorization.get();
    }

    public String lastBody() {
        return body.get();
    }

    public String lastUri() {
        return uri.get();
    }

    public String lastMethod() {
        return method.get();
    }

    @Override
    public void close() {
        server.stop(0);
    }
}