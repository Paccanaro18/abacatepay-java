package io.github.paccanaro18.abacatepay.webhook;

import io.github.paccanaro18.abacatepay.exception.WebhookVerificationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

public final class WebhookVerifier {

    private static final String ALGORITHM = "HmacSHA256";

    private final SecretKeySpec hmacKey;
    private final byte[] expectedSecret;

    public WebhookVerifier(String publicKey, String webhookSecret) {
        if (publicKey == null || publicKey.isBlank()) {
            throw new IllegalArgumentException("A chave pública do AbacatePay é obrigatória");
        }
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalArgumentException("O secret do webhook é obrigatório");
        }
        this.hmacKey = new SecretKeySpec(publicKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        this.expectedSecret = webhookSecret.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isSignatureValid(byte[] rawBody, String signatureFromHeader) {
        if (rawBody == null || signatureFromHeader == null || signatureFromHeader.isBlank()) {
            return false;
        }
        String expected = Base64.getEncoder().encodeToString(hmac(rawBody));
        return constantTimeEquals(
                expected.getBytes(StandardCharsets.UTF_8),
                signatureFromHeader.trim().getBytes(StandardCharsets.UTF_8));
    }

    public boolean isSecretValid(String secretFromRequest) {
        if (secretFromRequest == null) {
            return false;
        }
        return constantTimeEquals(expectedSecret, secretFromRequest.getBytes(StandardCharsets.UTF_8));
    }

    public void verify(byte[] rawBody, String signatureFromHeader, String secretFromRequest) {
        if (!isSecretValid(secretFromRequest)) {
            throw new WebhookVerificationException("Secret do webhook inválido");
        }
        if (!isSignatureValid(rawBody, signatureFromHeader)) {
            throw new WebhookVerificationException("Assinatura do webhook inválida");
        }
    }

    private byte[] hmac(byte[] data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(hmacKey);
            return mac.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 indisponível nesta JVM", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }
}