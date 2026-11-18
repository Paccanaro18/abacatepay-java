package io.github.paccanaro18.abacatepay;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.paccanaro18.abacatepay.checkouts.Checkout;
import io.github.paccanaro18.abacatepay.checkouts.CheckoutStatus;
import io.github.paccanaro18.abacatepay.checkouts.CreateCheckoutRequest;
import io.github.paccanaro18.abacatepay.exception.AbacatePayValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbacatePayTest {

    private FakeApi api;

    @BeforeEach
    void subirServidor() {
        api = new FakeApi();
    }

    @AfterEach
    void pararServidor() {
        api.close();
    }

    @Test
    void criaCobrancaEDevolveCheckout() throws Exception {
        api.respond("/checkouts/create", 200, """
                {
                  "success": true,
                  "error": null,
                  "data": {
                    "id": "bill_abc123xyz",
                    "externalId": "pedido-123",
                    "url": "https://app.abacatepay.com/pay/bill_abc123xyz",
                    "amount": 10000,
                    "status": "PENDING",
                    "devMode": true
                  }
                }
                """);

        AbacatePay abacate = new AbacatePay("abc_dev_123", api.baseUrl());

        Checkout checkout = abacate.checkouts().create(
                CreateCheckoutRequest.builder()
                        .item("prod_1", 1)
                        .externalId("pedido-123")
                        .build());

        assertEquals("bill_abc123xyz", checkout.id());
        assertEquals(CheckoutStatus.PENDING, checkout.status());
        assertTrue(checkout.devMode());

        assertEquals("Bearer abc_dev_123", api.lastAuthorization());
        JsonNode enviado = AbacatePayJson.mapper().readTree(api.lastBody());
        assertEquals("prod_1", enviado.get("items").get(0).get("id").asText());
        assertEquals("pedido-123", enviado.get("externalId").asText());
        assertFalse(enviado.has("customerId"));
    }

    @Test
    void erroDaApiChegaComoExcecaoEspecifica() {
        api.respond("/checkouts/create", 422, "{\"error\":\"items is required\"}");

        AbacatePay abacate = new AbacatePay("abc_dev_123", api.baseUrl());
        CreateCheckoutRequest request = CreateCheckoutRequest.builder().item("prod_1", 1).build();

        AbacatePayValidationException ex = assertThrows(
                AbacatePayValidationException.class,
                () -> abacate.checkouts().create(request));

        assertEquals("items is required", ex.getMessage());
    }

    @Test
    void chaveNulaFalhaCedo() {
        assertThrows(NullPointerException.class, () -> new AbacatePay(null));
    }

    @Test
    void urlPadraoApontaParaApiV2() {
        assertEquals("https://api.abacatepay.com/v2", AbacatePay.DEFAULT_BASE_URL);
    }
}