package io.github.paccanaro18.abacatepay.checkouts;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import io.github.paccanaro18.abacatepay.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutTest {

    private static final TypeReference<ApiResponse<Checkout>> TIPO = new TypeReference<>() {};

    private Checkout ler(String json) throws Exception {
        return AbacatePayJson.mapper().readValue(json, TIPO).data();
    }

    @Test
    void leRespostaDeCriacao() throws Exception {
        Checkout checkout = ler("""
                {
                  "data": {
                    "id": "bill_abc123xyz",
                    "externalId": "pedido-123",
                    "url": "https://app.abacatepay.com/pay/bill_abc123xyz",
                    "amount": 10000,
                    "paidAmount": null,
                    "items": [ { "id": "prod_456", "quantity": 2 } ],
                    "status": "PENDING",
                    "coupons": [],
                    "devMode": false,
                    "customerId": null,
                    "receiptUrl": null,
                    "metadata": {},
                    "createdAt": "2024-11-04T18:38:28.573Z"
                  },
                  "error": null,
                  "success": true
                }
                """);

        assertEquals("bill_abc123xyz", checkout.id());
        assertEquals("pedido-123", checkout.externalId());
        assertEquals(10000L, checkout.amount());
        assertNull(checkout.paidAmount());
        assertEquals(CheckoutStatus.PENDING, checkout.status());
        assertFalse(checkout.devMode());
        assertEquals(1, checkout.items().size());
        assertEquals("prod_456", checkout.items().get(0).id());
    }

    @Test
    void statusNovoDaApiViraUnknown() throws Exception {
        Checkout checkout = ler("""
                { "data": { "id": "bill_1", "status": "ALGO_NOVO" }, "error": null, "success": true }
                """);

        assertEquals(CheckoutStatus.UNKNOWN, checkout.status());
    }
}