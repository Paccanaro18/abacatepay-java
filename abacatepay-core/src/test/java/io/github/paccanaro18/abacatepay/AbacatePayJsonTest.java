package io.github.paccanaro18.abacatepay;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbacatePayJsonTest {

    record Exemplo(String a, String b) {}

    enum Cor {
        VERDE,
        @JsonEnumDefaultValue DESCONHECIDA
    }

    record Pintura(Cor cor) {}

    @Test
    void naoEnviaCamposNulos() throws Exception {
        String json = AbacatePayJson.mapper().writeValueAsString(new Exemplo("x", null));

        assertEquals("{\"a\":\"x\"}", json);
    }

    @Test
    void valorDeEnumDesconhecidoViraValorPadrao() throws Exception {
        Pintura pintura = AbacatePayJson.mapper().readValue("{\"cor\":\"ROXO\"}", Pintura.class);

        assertEquals(Cor.DESCONHECIDA, pintura.cor());
    }
}