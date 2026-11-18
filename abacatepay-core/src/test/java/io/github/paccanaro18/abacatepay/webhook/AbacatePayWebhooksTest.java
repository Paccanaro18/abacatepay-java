package io.github.paccanaro18.abacatepay.webhook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbacatePayWebhooksTest {

    @Test
    void chavePublicaEstaCompletaEBemFormatada() {
        String chave = AbacatePayWebhooks.PUBLIC_KEY;

        assertTrue(chave.length() > 200, "A chave tem mais de 200 caracteres na doc");
        assertTrue(chave.startsWith("t9dXRhHH"), "Começo diferente do que a doc mostra");
        assertTrue(chave.endsWith("HS1JhNHDX9"), "Final diferente do que a doc mostra");
        assertEquals(chave.strip(), chave, "Sobrou espaço no começo ou no fim");
        assertFalse(chave.contains(" "), "A chave não deve ter espaços");
        assertFalse(chave.contains("\n"), "A chave deve estar em uma linha só");
    }
}