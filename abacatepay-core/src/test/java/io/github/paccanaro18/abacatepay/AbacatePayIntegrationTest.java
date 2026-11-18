package io.github.paccanaro18.abacatepay;

import io.github.paccanaro18.abacatepay.checkouts.Checkout;
import io.github.paccanaro18.abacatepay.checkouts.CheckoutStatus;
import io.github.paccanaro18.abacatepay.checkouts.CreateCheckoutRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AbacatePayIntegrationTest {

    private static final Path ARQUIVO_LOCAL = Path.of("local-test.properties");

    private static String config(String nome) {
        String doAmbiente = System.getenv(nome);
        if (doAmbiente != null && !doAmbiente.isBlank()) {
            return doAmbiente.trim();
        }

        if (Files.exists(ARQUIVO_LOCAL)) {
            Properties props = new Properties();
            try (var entrada = Files.newInputStream(ARQUIVO_LOCAL)) {
                props.load(entrada);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            String doArquivo = props.getProperty(nome);
            if (doArquivo != null && !doArquivo.isBlank()) {
                return doArquivo.trim();
            }
        }
        return null;
    }

    @Test
    void criaCobrancaDeVerdadeNoModoTeste() {
        String apiKey = config("ABACATEPAY_API_KEY");
        String productId = config("ABACATEPAY_PRODUCT_ID");

        assumeTrue(apiKey != null,
                "Sem chave. Crie o arquivo " + ARQUIVO_LOCAL.toAbsolutePath()
                        + " (ou defina a variável de ambiente ABACATEPAY_API_KEY)");
        assumeTrue(apiKey.startsWith("abc_dev_"), "Só roda com chave do Modo Teste (abc_dev_)");
        assumeTrue(productId != null, "Defina ABACATEPAY_PRODUCT_ID");

        AbacatePay abacate = new AbacatePay(apiKey);

        Checkout checkout = abacate.checkouts().create(
                CreateCheckoutRequest.builder()
                        .item(productId, 1)
                        .externalId("sdk-teste-" + UUID.randomUUID())
                        .build());

        System.out.println("Abra para pagar (simulado): " + checkout.url());

        assertNotNull(checkout.id());
        assertEquals(CheckoutStatus.PENDING, checkout.status());
        assertTrue(checkout.devMode());
        assertTrue(checkout.url().startsWith("https://"));
    }
}