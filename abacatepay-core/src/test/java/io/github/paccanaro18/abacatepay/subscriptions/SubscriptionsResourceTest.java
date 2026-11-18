package io.github.paccanaro18.abacatepay.subscriptions;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.paccanaro18.abacatepay.AbacatePay;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import io.github.paccanaro18.abacatepay.FakeApi;
import io.github.paccanaro18.abacatepay.Page;
import io.github.paccanaro18.abacatepay.checkouts.Checkout;
import io.github.paccanaro18.abacatepay.checkouts.CheckoutStatus;
import io.github.paccanaro18.abacatepay.checkouts.PaymentMethod;
import io.github.paccanaro18.abacatepay.exception.AbacatePayNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionsResourceTest {

    private static final String ASSINATURA_JSON = """
            {
              "data": {
                "id": "subs_abc123xyz",
                "checkoutId": "bill_abc123xyz",
                "customerId": "cust_abc123xyz",
                "amount": 10000,
                "status": "ACTIVE",
                "method": "CARD",
                "coupons": [],
                "devMode": false,
                "trialDays": null,
                "trialEndsAt": null,
                "retryPolicy": { "maxRetry": 3, "retryEvery": 1 },
                "createdAt": "2024-11-04T18:38:28.573Z",
                "updatedAt": "2024-11-04T18:38:28.573Z"
              },
              "error": null,
              "success": true
            }
            """;

    private static final String CHECKOUT_JSON = """
            {
              "data": {
                "id": "bill_abc123xyz",
                "externalId": "assinatura-123",
                "url": "https://app.abacatepay.com/pay/bill_abc123xyz",
                "amount": 10000,
                "paidAmount": null,
                "items": [{ "id": "prod_456", "quantity": 1 }],
                "status": "PENDING",
                "coupons": [],
                "devMode": false,
                "customerId": null,
                "card": { "maxInstallments": 1, "passFeesToCustomer": false },
                "customerFee": null,
                "metadata": {},
                "createdAt": "2024-11-04T18:38:28.573Z",
                "updatedAt": "2024-11-04T18:38:28.573Z"
              },
              "error": null,
              "success": true
            }
            """;

    private FakeApi api;
    private AbacatePay abacate;

    @BeforeEach
    void subirServidor() {
        api = new FakeApi();
        abacate = new AbacatePay("abc_dev_123", api.baseUrl());
    }

    @AfterEach
    void pararServidor() {
        api.close();
    }

    @Test
    void criaCheckoutDeAssinaturaEDevolveOCheckout() throws Exception {
        api.respond("/subscriptions/create", 200, CHECKOUT_JSON);

        Checkout checkout = abacate.subscriptions().createCheckout(CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1234", 1)
                .externalId("assinatura-123")
                .retryPolicy(3, 2)
                .build());

        assertEquals("POST", api.lastMethod());
        assertEquals("/subscriptions/create", api.lastUri());
        assertEquals("Bearer abc_dev_123", api.lastAuthorization());
        JsonNode enviado = AbacatePayJson.mapper().readTree(api.lastBody());
        assertEquals("prod-1234", enviado.get("items").get(0).get("id").asText());
        assertEquals(2, enviado.get("retryPolicy").get("retryEvery").asInt());

        assertEquals("bill_abc123xyz", checkout.id());
        assertEquals(CheckoutStatus.PENDING, checkout.status());
        assertEquals("https://app.abacatepay.com/pay/bill_abc123xyz", checkout.url());
    }

    @Test
    void buscaPorIdPorExternalIdEPorCliente() {
        api.respond("/subscriptions/get", 200, ASSINATURA_JSON);

        Subscription assinatura = abacate.subscriptions().get("subs_abc123xyz");
        assertEquals("GET", api.lastMethod());
        assertEquals("/subscriptions/get?id=subs_abc123xyz", api.lastUri());
        assertEquals("subs_abc123xyz", assinatura.id());
        assertEquals("bill_abc123xyz", assinatura.checkoutId());
        assertEquals("cust_abc123xyz", assinatura.customerId());
        assertEquals(10000L, assinatura.amount());
        assertEquals(SubscriptionStatus.ACTIVE, assinatura.status());
        assertEquals(PaymentMethod.CARD, assinatura.method());
        assertNull(assinatura.trialDays());
        assertNull(assinatura.trialEndsAt());
        assertEquals(java.time.Instant.parse("2024-11-04T18:38:28.573Z"), assinatura.createdAt());
        assertEquals(3, assinatura.retryPolicy().maxRetry());
        assertEquals(1, assinatura.retryPolicy().retryEvery());

        abacate.subscriptions().getByExternalId("assinatura-123");
        assertEquals("/subscriptions/get?externalId=assinatura-123", api.lastUri());

        abacate.subscriptions().getByCustomerId("cust_abc123xyz");
        assertEquals("/subscriptions/get?customerId=cust_abc123xyz", api.lastUri());
    }

    @Test
    void assinaturaInexistenteLancaNotFound() {
        api.respond("/subscriptions/get", 404, """
                { "success": false, "data": null, "error": "Subscription not found" }
                """);

        AbacatePayNotFoundException ex = assertThrows(
                AbacatePayNotFoundException.class,
                () -> abacate.subscriptions().get("subs_naoexiste"));

        assertEquals("Subscription not found", ex.getMessage());
    }

    @Test
    void listaDevolvePaginaComCursores() {
        api.respond("/subscriptions/list", 200, """
                {
                  "data": [
                    { "id": "subs_1", "checkoutId": "bill_1", "customerId": "cust_1", "amount": 4990,
                      "status": "PENDING", "method": "PIX", "coupons": [], "devMode": true,
                      "trialDays": 7, "trialEndsAt": "2024-11-11T18:38:28.573Z",
                      "retryPolicy": { "maxRetry": 3, "retryEvery": 1 } }
                  ],
                  "success": true,
                  "error": null,
                  "pagination": { "hasMore": true, "next": "subs_1", "before": null }
                }
                """);

        Page<Subscription> pagina = abacate.subscriptions().list(
                ListSubscriptionsRequest.builder().status(SubscriptionStatus.ACTIVE).limit(10).build());

        assertEquals("/subscriptions/list?limit=10&status=ACTIVE", api.lastUri());
        assertEquals(SubscriptionStatus.PENDING, pagina.items().get(0).status());
        assertEquals(PaymentMethod.PIX, pagina.items().get(0).method());
        assertEquals(7, pagina.items().get(0).trialDays());
        assertEquals(java.time.Instant.parse("2024-11-11T18:38:28.573Z"), pagina.items().get(0).trialEndsAt());
        assertTrue(pagina.hasMore());
        assertEquals("subs_1", pagina.next());
        assertNull(pagina.before());
    }

    @Test
    void listaSemFiltrosNaoMandaQuery() {
        api.respond("/subscriptions/list", 200, """
                { "data": [], "success": true, "error": null,
                  "pagination": { "hasMore": false, "next": null, "before": null } }
                """);

        Page<Subscription> pagina = abacate.subscriptions().list();

        assertEquals("/subscriptions/list", api.lastUri());
        assertTrue(pagina.items().isEmpty());
    }

    @Test
    void cancelaComIdNoCorpoJson() throws Exception {
        api.respond("/subscriptions/cancel", 200, ASSINATURA_JSON.replace("\"ACTIVE\"", "\"CANCELLED\""));

        Subscription cancelada = abacate.subscriptions().cancel("subs_abc123xyz");

        assertEquals("POST", api.lastMethod());
        assertEquals("/subscriptions/cancel", api.lastUri());
        JsonNode enviado = AbacatePayJson.mapper().readTree(api.lastBody());
        assertEquals("subs_abc123xyz", enviado.get("id").asText());
        assertEquals(1, enviado.size());
        assertEquals(SubscriptionStatus.CANCELLED, cancelada.status());
    }

    @Test
    void excluiCheckoutComPostSemCorpoEIdNaQuery() {
        api.respond("/subscriptions/checkouts/delete", 200, CHECKOUT_JSON);

        Checkout excluido = abacate.subscriptions().deleteCheckout("bill_abc123xyz");

        assertEquals("POST", api.lastMethod());
        assertEquals("/subscriptions/checkouts/delete?id=bill_abc123xyz", api.lastUri());
        assertEquals("", api.lastBody());
        assertEquals("bill_abc123xyz", excluido.id());
    }

    @Test
    void identificadorEmBrancoFalhaAntesDeChamarAApi() {
        assertThrows(IllegalArgumentException.class, () -> abacate.subscriptions().get(" "));
        assertThrows(IllegalArgumentException.class, () -> abacate.subscriptions().getByExternalId(null));
        assertThrows(IllegalArgumentException.class, () -> abacate.subscriptions().getByCustomerId(""));
        assertThrows(IllegalArgumentException.class, () -> abacate.subscriptions().cancel(" "));
        assertThrows(IllegalArgumentException.class, () -> abacate.subscriptions().deleteCheckout(null));
    }
}
