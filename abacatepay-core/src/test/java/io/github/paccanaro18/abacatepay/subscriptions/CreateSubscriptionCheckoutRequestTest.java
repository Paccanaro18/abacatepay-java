package io.github.paccanaro18.abacatepay.subscriptions;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import io.github.paccanaro18.abacatepay.checkouts.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CreateSubscriptionCheckoutRequestTest {

    @Test
    void montaORequestEOmiteCamposNaoInformados() throws Exception {
        CreateSubscriptionCheckoutRequest request = CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1234", 1)
                .customerId("cust_abc123xyz")
                .externalId("assinatura-123")
                .methods(PaymentMethod.CARD)
                .card(1, false)
                .retryPolicy(3, 2)
                .build();

        JsonNode json = AbacatePayJson.mapper().valueToTree(request);

        assertEquals("prod-1234", json.get("items").get(0).get("id").asText());
        assertEquals(1, json.get("items").get(0).get("quantity").asInt());
        assertEquals("CARD", json.get("methods").get(0).asText());
        assertEquals(1, json.get("card").get("maxInstallments").asInt());
        assertFalse(json.get("card").get("passFeesToCustomer").asBoolean());
        assertEquals(3, json.get("retryPolicy").get("maxRetry").asInt());
        assertEquals(2, json.get("retryPolicy").get("retryEvery").asInt());
        assertFalse(json.has("returnUrl"));
        assertFalse(json.has("coupons"));
    }

    @Test
    void exigeExatamenteUmItem() {
        assertThrows(IllegalArgumentException.class,
                () -> CreateSubscriptionCheckoutRequest.builder().build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).item("prod-2", 1).build());
    }

    @Test
    void itemPrecisaDeIdEQuantidadeValidos() {
        assertThrows(IllegalArgumentException.class,
                () -> CreateSubscriptionCheckoutRequest.builder().item(" ", 1).build());
        assertThrows(IllegalArgumentException.class,
                () -> CreateSubscriptionCheckoutRequest.builder().item("prod-1", 0).build());
    }

    @Test
    void metodosAceitosSaoPixCartaoEBoleto() {
        assertDoesNotThrow(() -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.PIX, PaymentMethod.CARD).build());
        assertDoesNotThrow(() -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.BOLETO).build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.UNKNOWN).build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods().build());
    }

    @Test
    void parcelasDoCartaoFicamEntre1E12() {
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).card(0, false).build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).card(13, false).build());
        assertDoesNotThrow(() -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).card(12, true).build());
    }

    @Test
    void cartaoSoComMetodoCartao() {
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.PIX).card(1, false).build());
    }

    @Test
    void politicaDeRetentativaTemLimites() {
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).retryPolicy(0, 1).build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).retryPolicy(11, 1).build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).retryPolicy(3, 0).build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).retryPolicy(3, 31).build());
    }

    @Test
    void aceitaNoMaximo50Cupons() {
        String[] cupons = Collections.nCopies(51, "CUPOM").toArray(new String[0]);

        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).coupons(cupons).build());
    }

    @Test
    void camposDeBoletoVaoNoJson() throws Exception {
        LocalDate vencimento = LocalDate.now().plusDays(30);
        CreateSubscriptionCheckoutRequest request = CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1)
                .methods(PaymentMethod.BOLETO)
                .dueDate(vencimento)
                .interest(100)
                .fine(200, FineType.PERCENTAGE)
                .upSellProductId("prod_bump456xyz")
                .build();

        JsonNode json = AbacatePayJson.mapper().valueToTree(request);

        assertEquals(vencimento.toString(), json.get("dueDate").asText());
        assertEquals(100, json.get("interest").get("value").asInt());
        assertEquals(200, json.get("fine").get("value").asInt());
        assertEquals("PERCENTAGE", json.get("fine").get("type").asText());
        assertEquals("prod_bump456xyz", json.get("upSellProductId").asText());
    }

    @Test
    void vencimentoJurosEMultaExigemBoleto() {
        LocalDate vencimento = LocalDate.now().plusDays(10);

        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).dueDate(vencimento).build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.CARD).interest(100).build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.PIX).fine(1000, FineType.FIXED).build());
    }

    @Test
    void vencimentoNaoPodeEstarNoPassadoNemPassarDe365Dias() {
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.BOLETO).dueDate(LocalDate.now().minusDays(1)).build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.BOLETO).dueDate(LocalDate.now().plusDays(366)).build());
        assertDoesNotThrow(() -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.BOLETO).dueDate(LocalDate.now()).build());
    }

    @Test
    void jurosEMultaNaoPodemSerNegativos() {
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.BOLETO).interest(-1).build());
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).methods(PaymentMethod.BOLETO).fine(-1, FineType.FIXED).build());
    }

    @Test
    void produtoDeUpsellNaoPodeSerEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> CreateSubscriptionCheckoutRequest.builder()
                .item("prod-1", 1).upSellProductId(" ").build());
    }
}
