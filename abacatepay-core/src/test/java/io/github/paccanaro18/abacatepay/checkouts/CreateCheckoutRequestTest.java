package io.github.paccanaro18.abacatepay.checkouts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreateCheckoutRequestTest {

    private final ObjectMapper mapper = AbacatePayJson.mapper();

    private JsonNode paraJson(CreateCheckoutRequest request) throws Exception {
        return mapper.readTree(mapper.writeValueAsString(request));
    }

    @Test
    void requisicaoMinimaTemSoItems() throws Exception {
        CreateCheckoutRequest request = CreateCheckoutRequest.builder()
                .item("prod_abc123xyz", 1)
                .build();

        JsonNode esperado = mapper.readTree("""
                { "items": [ { "id": "prod_abc123xyz", "quantity": 1 } ] }
                """);

        assertEquals(esperado, paraJson(request));
    }

    @Test
    void requisicaoCompletaEnviaTodosOsCampos() throws Exception {
        CreateCheckoutRequest request = CreateCheckoutRequest.builder()
                .item("prod_1", 2)
                .methods(PaymentMethod.PIX, PaymentMethod.CARD)
                .externalId("pedido-123")
                .customerId("cust_abc123")
                .returnUrl("https://seusite.com/carrinho")
                .completionUrl("https://seusite.com/obrigado")
                .coupons("PROMO10")
                .metadata(Map.of("origem", "app-mobile"))
                .build();

        JsonNode json = paraJson(request);

        assertEquals(2, json.get("items").get(0).get("quantity").asInt());
        assertEquals("PIX", json.get("methods").get(0).asText());
        assertEquals("CARD", json.get("methods").get(1).asText());
        assertEquals("pedido-123", json.get("externalId").asText());
        assertEquals("cust_abc123", json.get("customerId").asText());
        assertEquals("PROMO10", json.get("coupons").get(0).asText());
        assertEquals("app-mobile", json.get("metadata").get("origem").asText());
    }

    @Test
    void semItemsNaoPodeSerCriada() {
        assertThrows(IllegalArgumentException.class,
                () -> CreateCheckoutRequest.builder().build());
    }

    @Test
    void quantidadeMenorQueUmNaoPodeSerCriada() {
        assertThrows(IllegalArgumentException.class,
                () -> CreateCheckoutRequest.builder().item("prod_1", 0).build());
    }

    @Test
    void listaDeMetodosVaziaNaoPodeSerCriada() {
        assertThrows(IllegalArgumentException.class,
                () -> CreateCheckoutRequest.builder().item("prod_1", 1).methods().build());
    }
    @Test
    void metodoUnknownNaoPodeSerEnviadoParaAApi() {
        assertThrows(IllegalArgumentException.class,
                () -> CreateCheckoutRequest.builder()
                        .item("prod_1", 1)
                        .methods(PaymentMethod.UNKNOWN)
                        .build());
    }
}