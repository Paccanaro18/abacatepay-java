package io.github.paccanaro18.abacatepay.checkouts;

import io.github.paccanaro18.abacatepay.AbacatePay;
import io.github.paccanaro18.abacatepay.FakeApi;
import io.github.paccanaro18.abacatepay.Page;
import io.github.paccanaro18.abacatepay.exception.AbacatePayNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutsResourceTest {

    private static final String CHECKOUT_JSON = """
            {
              "data": {
                "id": "bill_abc123xyz",
                "externalId": "pedido-123",
                "url": "https://app.abacatepay.com/pay/bill_abc123xyz",
                "amount": 10000,
                "paidAmount": 10000,
                "items": [ { "id": "prod_456", "quantity": 2 } ],
                "status": "PAID",
                "coupons": ["BEMVINDO"],
                "devMode": false,
                "customerId": "cust_abc123xyz",
                "createdAt": "2024-11-04T18:38:28.573Z",
                "updatedAt": "2024-11-05T10:00:00.000Z"
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
    void buscaCheckoutPeloId() {
        api.respond("/checkouts/get", 200, CHECKOUT_JSON);

        Checkout checkout = abacate.checkouts().get("bill_abc123xyz");

        assertEquals("GET", api.lastMethod());
        assertEquals("/checkouts/get?id=bill_abc123xyz", api.lastUri());
        assertEquals("Bearer abc_dev_123", api.lastAuthorization());
        assertEquals("bill_abc123xyz", checkout.id());
        assertEquals(CheckoutStatus.PAID, checkout.status());
        assertEquals(10000L, checkout.paidAmount());
        assertEquals("BEMVINDO", checkout.coupons().get(0));
        assertEquals("2024-11-04T18:38:28.573Z", checkout.createdAt());
    }

    @Test
    void buscaCheckoutPeloExternalId() {
        api.respond("/checkouts/get", 200, CHECKOUT_JSON);

        abacate.checkouts().getByExternalId("pedido-123");

        assertEquals("/checkouts/get?externalId=pedido-123", api.lastUri());
    }

    @Test
    void checkoutInexistenteLancaNotFound() {
        api.respond("/checkouts/get", 404, """
                { "success": false, "data": null, "error": "Billing not found" }
                """);

        AbacatePayNotFoundException ex = assertThrows(
                AbacatePayNotFoundException.class,
                () -> abacate.checkouts().get("bill_naoexiste"));

        assertEquals(404, ex.statusCode());
        assertEquals("Billing not found", ex.getMessage());
    }

    @Test
    void idEmBrancoFalhaAntesDeChamarAApi() {
        assertThrows(IllegalArgumentException.class, () -> abacate.checkouts().get(" "));
        assertThrows(IllegalArgumentException.class, () -> abacate.checkouts().getByExternalId(null));
    }

    @Test
    void listaDevolvePaginaComCursor() {
        api.respond("/checkouts/list", 200, """
                {
                  "data": [
                    { "id": "bill_1", "amount": 1000, "status": "PAID", "items": [] },
                    { "id": "bill_2", "amount": 2000, "status": "PENDING", "items": [] }
                  ],
                  "success": true,
                  "error": null,
                  "pagination": { "hasMore": true, "next": "cur_2", "before": null }
                }
                """);

        Page<Checkout> pagina = abacate.checkouts().list(
                ListCheckoutsRequest.builder().limit(2).status(CheckoutStatus.PAID).build());

        assertEquals("/checkouts/list?limit=2&status=PAID", api.lastUri());
        assertEquals(2, pagina.items().size());
        assertEquals("bill_1", pagina.items().get(0).id());
        assertTrue(pagina.hasMore());
        assertEquals("cur_2", pagina.next());
        assertNull(pagina.before());
    }

    @Test
    void listaSemFiltrosNaoEnviaQuery() {
        api.respond("/checkouts/list", 200, """
                { "data": [], "success": true, "error": null,
                  "pagination": { "hasMore": false, "next": null, "before": null } }
                """);

        Page<Checkout> pagina = abacate.checkouts().list();

        assertEquals("/checkouts/list", api.lastUri());
        assertTrue(pagina.items().isEmpty());
        assertFalse(pagina.hasMore());
        assertNull(pagina.next());
    }

    @Test
    void respostaSemPaginationViraPaginaSemProximaPagina() {
        api.respond("/checkouts/list", 200, """
                { "data": [ { "id": "bill_1", "amount": 1000, "status": "PAID", "items": [] } ],
                  "success": true, "error": null }
                """);

        Page<Checkout> pagina = abacate.checkouts().list();

        assertEquals(1, pagina.items().size());
        assertFalse(pagina.hasMore());
    }
}
