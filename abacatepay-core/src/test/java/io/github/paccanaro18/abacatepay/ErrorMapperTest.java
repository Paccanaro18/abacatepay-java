package io.github.paccanaro18.abacatepay.http;

import io.github.paccanaro18.abacatepay.exception.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorMapperTest {

    @Test
    void mapeia401ParaAutenticacao() {
        AbacatePayException ex = ErrorMapper.map(
                401, "{\"error\":\"Invalid or inactive API key\"}", null);

        assertInstanceOf(AbacatePayAuthenticationException.class, ex);
        assertEquals(401, ex.statusCode());
        assertEquals("Invalid or inactive API key", ex.getMessage());
    }

    @Test
    void mapeia422ParaValidacao() {
        AbacatePayException ex = ErrorMapper.map(
                422, "{\"success\":false,\"data\":null,\"error\":\"items is required\"}", null);

        assertInstanceOf(AbacatePayValidationException.class, ex);
        assertEquals("items is required", ex.getMessage());
    }

    @Test
    void mapeia400ParaRegraDeNegocio() {
        AbacatePayException ex = ErrorMapper.map(400, "{\"error\":\"Product not found\"}", null);

        assertInstanceOf(AbacatePayBusinessException.class, ex);
    }

    @Test
    void mapeia429GuardandoTempoDeEspera() {
        AbacatePayException ex = ErrorMapper.map(429, "{\"error\":\"Too many requests\"}", 7L);

        AbacatePayRateLimitException rateLimit =
                assertInstanceOf(AbacatePayRateLimitException.class, ex);
        assertEquals(7L, rateLimit.retryAfterSeconds());
    }

    @Test
    void mapeia5xxParaServidorEUsaMensagemPadraoQuandoCorpoNaoEJson() {
        AbacatePayException ex = ErrorMapper.map(502, "<html>Bad Gateway</html>", null);

        assertInstanceOf(AbacatePayServerException.class, ex);
        assertEquals(502, ex.statusCode());
        assertEquals("HTTP 502", ex.getMessage());
    }

    @Test
    void usaMensagemPadraoQuandoCorpoVemVazio() {
        AbacatePayException ex = ErrorMapper.map(500, "", null);

        assertEquals("HTTP 500", ex.getMessage());
    }
}