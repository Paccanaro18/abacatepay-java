package io.github.paccanaro18.abacatepay.webhook;

import io.github.paccanaro18.abacatepay.checkouts.CheckoutStatus;
import io.github.paccanaro18.abacatepay.checkouts.PaymentMethod;
import io.github.paccanaro18.abacatepay.exception.WebhookParseException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class WebhookParserTest {

    private static byte[] bytes(String texto) {
        return texto.getBytes(StandardCharsets.UTF_8);
    }

    private static final String CHECKOUT_COMPLETED_PIX = """
            {
              "id": "evt_abc123",
              "event": "checkout.completed",
              "apiVersion": 2,
              "devMode": true,
              "data": {
                "checkout": {
                  "id": "bill_abc123xyz",
                  "externalId": "pedido-123",
                  "url": "https://app.abacatepay.com/pay/bill_abc123xyz",
                  "amount": 10000,
                  "paidAmount": 10000,
                  "platformFee": 80,
                  "frequency": "ONE_TIME",
                  "items": [ { "id": "prod_xyz", "quantity": 1 } ],
                  "status": "PAID",
                  "methods": [ "PIX" ],
                  "customerId": "cust_abc123",
                  "receiptUrl": "https://app.abacatepay.com/receipt/abc",
                  "installmentsCount": null,
                  "createdAt": "2024-12-06T18:56:15.538Z",
                  "updatedAt": "2024-12-06T18:56:20.000Z"
                },
                "customer": {
                  "id": "cust_abc123",
                  "name": "João Silva",
                  "email": "joao@exemplo.com",
                  "taxId": "123.***.***-**"
                },
                "payerInformation": {
                  "method": "PIX",
                  "PIX": { "name": "João Silva", "taxId": "123.***.***-**", "isSameAsCustomer": true }
                }
              }
            }
            """;

    private static final String CHECKOUT_REFUNDED_CARD = """
            {
              "id": "evt_def456",
              "event": "checkout.refunded",
              "apiVersion": 2,
              "devMode": false,
              "data": {
                "checkout": {
                  "id": "bill_card1",
                  "externalId": "pedido-456",
                  "amount": 30000,
                  "paidAmount": 30000,
                  "platformFee": 120,
                  "status": "REFUNDED",
                  "methods": [ "CARD" ],
                  "installmentsCount": 3
                },
                "customer": null,
                "payerInformation": {
                  "method": "CARD",
                  "CARD": { "number": "4242", "brand": "VISA" }
                },
                "reason": "requested_by_customer"
              }
            }
            """;

    @Test
    void leEventoDeCheckoutConcluidoPagoNoPix() {
        WebhookEvent event = WebhookParser.parse(bytes(CHECKOUT_COMPLETED_PIX));

        assertEquals("evt_abc123", event.id());
        assertEquals(WebhookEventType.CHECKOUT_COMPLETED, event.type());
        assertEquals(2, event.apiVersion());
        assertTrue(event.devMode());

        CheckoutEventData data = event.checkoutData();
        assertEquals("bill_abc123xyz", data.checkout().id());
        assertEquals("pedido-123", data.checkout().externalId());
        assertEquals(10000L, data.checkout().amount());
        assertEquals(10000L, data.checkout().paidAmount());
        assertEquals(80L, data.checkout().platformFee());
        assertEquals(CheckoutStatus.PAID, data.checkout().status());
        assertEquals(java.util.List.of(PaymentMethod.PIX), data.checkout().methods());

        assertEquals("João Silva", data.customer().name());

        assertEquals(PaymentMethod.PIX, data.payerInformation().method());
        assertEquals("João Silva", data.payerInformation().pix().name());
        assertTrue(data.payerInformation().pix().isSameAsCustomer());
        assertNull(data.payerInformation().card());

        assertNull(data.reason());
    }

    @Test
    void leEstornoNoCartaoComMotivoEClienteNulo() {
        CheckoutEventData data = WebhookParser.parse(bytes(CHECKOUT_REFUNDED_CARD)).checkoutData();

        assertEquals(CheckoutStatus.REFUNDED, data.checkout().status());
        assertEquals(3, data.checkout().installmentsCount());
        assertNull(data.customer());
        assertEquals("VISA", data.payerInformation().card().brand());
        assertEquals("4242", data.payerInformation().card().number());
        assertEquals("requested_by_customer", data.reason());
    }

    @Test
    void eventoDeOutroTipoViraUnknownMasPreservaONome() {
        WebhookEvent event = WebhookParser.parse(bytes("""
                { "id": "evt_1", "event": "transfer.completed", "apiVersion": 2, "devMode": false, "data": {} }
                """));

        assertEquals(WebhookEventType.UNKNOWN, event.type());
        assertEquals("transfer.completed", event.event());
        assertThrows(IllegalStateException.class, event::checkoutData);
    }

    @Test
    void corpoQueNaoEJsonLancaExcecaoClara() {
        WebhookParseException ex = assertThrows(
                WebhookParseException.class,
                () -> WebhookParser.parse(bytes("<html>oops</html>")));

        assertEquals(400, ex.statusCode());
        assertNotNull(ex.getCause());
    }

    @Test
    void corpoVazioOuSemCampoEventLancaExcecao() {
        assertThrows(WebhookParseException.class, () -> WebhookParser.parse(new byte[0]));
        assertThrows(WebhookParseException.class, () -> WebhookParser.parse(null));
        assertThrows(WebhookParseException.class, () -> WebhookParser.parse(bytes("{}")));
    }
}