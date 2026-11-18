package io.github.paccanaro18.abacatepay.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryParamsTest {

    @Test
    void semParametrosDevolveTextoVazio() {
        assertEquals("", new QueryParams().build());
    }

    @Test
    void montaComInterrogacaoEECommercial() {
        assertEquals("?a=1&b=dois", new QueryParams().add("a", 1).add("b", "dois").build());
    }

    @Test
    void ignoraValoresNulosEEmBranco() {
        assertEquals("?a=1", new QueryParams().add("x", null).add("y", "  ").add("a", "1").build());
    }

    @Test
    void codificaCaracteresEspeciais() {
        assertEquals("?q=a+b%26c", new QueryParams().add("q", "a b&c").build());
    }
}
