package io.github.paccanaro18.abacatepay.webhook;

import io.github.paccanaro18.abacatepay.exception.WebhookVerificationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class WebhookVerifierTest {

    private static final String CHAVE = "chave-publica-de-teste";
    private static final String SECRET = "meu-secret";

    private static final String CORPO =
            "{\"event\":\"checkout.completed\",\"id\":\"evt_123\"}";
    private static final String ASSINATURA_DO_CORPO =
            "jVYDeStZ7MLk3CER9H4OYe80UoAF2/1SHFePGZtNTf0=";


    private static final String CORPO_COM_ESPACOS =
            "{ \"event\":  \"checkout.completed\",\n  \"id\": \"evt_123\" }";
    private static final String ASSINATURA_DO_CORPO_COM_ESPACOS =
            "JzG9XMVq5R/QBrAxlCG/SrK5g8NMX8SdqKGFpsfmT44=";


    private static final String ASSINATURA_DE_OUTRA_CHAVE =
            "6p0hXr5/upRpzBSymNyHxxgXbOQCfziJ+MLt9cqMj9Q=";

    private final WebhookVerifier verifier = new WebhookVerifier(CHAVE, SECRET);

    private static byte[] bytes(String texto) {
        return texto.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void assinaturaCorretaEhAceita() {
        assertTrue(verifier.isSignatureValid(bytes(CORPO), ASSINATURA_DO_CORPO));
    }

    @Test
    void assinaturaValeParaOsBytesExatosDoCorpo() {
        assertTrue(verifier.isSignatureValid(
                bytes(CORPO_COM_ESPACOS), ASSINATURA_DO_CORPO_COM_ESPACOS));

        // Mesmo JSON "logicamente", bytes diferentes: a assinatura não bate
        assertFalse(verifier.isSignatureValid(
                bytes(CORPO), ASSINATURA_DO_CORPO_COM_ESPACOS));
    }

    @Test
    void corpoAlteradoEhRecusado() {
        byte[] adulterado = bytes(CORPO.replace("evt_123", "evt_124"));

        assertFalse(verifier.isSignatureValid(adulterado, ASSINATURA_DO_CORPO));
    }

    @Test
    void assinaturaFeitaComOutraChaveEhRecusada() {
        assertFalse(verifier.isSignatureValid(bytes(CORPO), ASSINATURA_DE_OUTRA_CHAVE));
    }

    @Test
    void assinaturaNulaOuVaziaEhRecusada() {
        assertFalse(verifier.isSignatureValid(bytes(CORPO), null));
        assertFalse(verifier.isSignatureValid(bytes(CORPO), ""));
        assertFalse(verifier.isSignatureValid(bytes(CORPO), "   "));
        assertFalse(verifier.isSignatureValid(null, ASSINATURA_DO_CORPO));
    }

    @Test
    void secretCorretoEhAceito() {
        assertTrue(verifier.isSecretValid(SECRET));
    }

    @Test
    void secretErradoOuNuloEhRecusado() {
        assertFalse(verifier.isSecretValid("outro-secret"));
        assertFalse(verifier.isSecretValid(""));
        assertFalse(verifier.isSecretValid(null));
    }

    @Test
    void verifyAceitaQuandoSecretEAssinaturaEstaoCertos() {
        assertDoesNotThrow(() ->
                verifier.verify(bytes(CORPO), ASSINATURA_DO_CORPO, SECRET));
    }

    @Test
    void verifyRecusaSecretInvalido() {
        WebhookVerificationException ex = assertThrows(
                WebhookVerificationException.class,
                () -> verifier.verify(bytes(CORPO), ASSINATURA_DO_CORPO, "errado"));

        assertEquals(401, ex.statusCode());
        assertEquals("Secret do webhook inválido", ex.getMessage());
    }

    @Test
    void verifyRecusaAssinaturaInvalida() {
        WebhookVerificationException ex = assertThrows(
                WebhookVerificationException.class,
                () -> verifier.verify(bytes(CORPO), ASSINATURA_DE_OUTRA_CHAVE, SECRET));

        assertEquals(401, ex.statusCode());
        assertEquals("Assinatura do webhook inválida", ex.getMessage());
    }

    @Test
    void construtorRecusaChaveOuSecretVazios() {
        assertThrows(IllegalArgumentException.class, () -> new WebhookVerifier("", SECRET));
        assertThrows(IllegalArgumentException.class, () -> new WebhookVerifier(null, SECRET));
        assertThrows(IllegalArgumentException.class, () -> new WebhookVerifier(CHAVE, " "));
        assertThrows(IllegalArgumentException.class, () -> new WebhookVerifier(CHAVE, null));
    }
}