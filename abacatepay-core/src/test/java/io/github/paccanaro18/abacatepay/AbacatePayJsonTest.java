package io.github.paccanaro18.abacatepay;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

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

    record Datas(Instant momento, LocalDate dia) {}

    @Test
    void leDatasIsoComoInstantELocalDate() throws Exception {
        Datas datas = AbacatePayJson.mapper().readValue(
                "{\"momento\":\"2024-11-04T18:38:28.573Z\",\"dia\":\"2026-08-15\"}", Datas.class);

        assertEquals(Instant.parse("2024-11-04T18:38:28.573Z"), datas.momento());
        assertEquals(LocalDate.of(2026, 8, 15), datas.dia());
    }

    @Test
    void escreveDatasComoTextoIsoENaoComoNumero() throws Exception {
        String json = AbacatePayJson.mapper().writeValueAsString(
                new Datas(Instant.parse("2024-11-04T18:38:28.573Z"), LocalDate.of(2026, 8, 15)));

        assertEquals("{\"momento\":\"2024-11-04T18:38:28.573Z\",\"dia\":\"2026-08-15\"}", json);
    }
}
