package io.github.paccanaro18.abacatepay.products;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateProductRequestTest {

    private static CreateProductRequest.Builder base() {
        return CreateProductRequest.builder()
                .externalId("prod-123")
                .name("Camiseta Premium")
                .price(8990);
    }

    @Test
    void moedaPadraoEhBrl() {
        assertEquals("BRL", base().build().currency());
    }

    @Test
    void serializaSemCamposNulos() throws Exception {
        JsonNode json = AbacatePayJson.mapper().readTree(AbacatePayJson.mapper().writeValueAsString(base().build()));

        assertEquals("prod-123", json.get("externalId").asText());
        assertEquals(8990, json.get("price").asLong());
        assertEquals("BRL", json.get("currency").asText());
        assertFalse(json.has("description"));
        assertFalse(json.has("cycle"));
        assertFalse(json.has("trialDays"));
    }

    @Test
    void aceitaAssinaturaComPeriodoDeTeste() {
        CreateProductRequest request = base().cycle(BillingCycle.MONTHLY).trialDays(7).build();

        assertEquals(BillingCycle.MONTHLY, request.cycle());
        assertEquals(7, request.trialDays());
    }

    @Test
    void externalIdENomeSaoObrigatorios() {
        assertThrows(IllegalArgumentException.class, () -> base().externalId(" ").build());
        assertThrows(IllegalArgumentException.class, () -> base().name(null).build());
    }

    @Test
    void precoPrecisaSerDePeloMenosUmCentavo() {
        assertThrows(IllegalArgumentException.class, () -> base().price(0).build());
        assertThrows(IllegalArgumentException.class, () -> base().price(-5).build());
    }

    @Test
    void trialDaysExigeCicloEFicaEntre1E90() {
        assertThrows(IllegalArgumentException.class, () -> base().trialDays(7).build());
        assertThrows(IllegalArgumentException.class,
                () -> base().cycle(BillingCycle.MONTHLY).trialDays(0).build());
        assertThrows(IllegalArgumentException.class,
                () -> base().cycle(BillingCycle.MONTHLY).trialDays(91).build());
    }

    @Test
    void cicloUnknownNaoPodeSerEnviado() {
        assertThrows(IllegalArgumentException.class, () -> base().cycle(BillingCycle.UNKNOWN).build());
    }
}
