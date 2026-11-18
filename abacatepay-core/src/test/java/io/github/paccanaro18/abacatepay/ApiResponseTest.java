package io.github.paccanaro18.abacatepay;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    record Bill(String id, String status, long amount) {}

    private final ObjectMapper mapper = AbacatePayJson.mapper();

    @Test
    void leRespostaDeSucesso() throws Exception {
        String json = """
                {
                  "data": { "id": "bill_abc123xyz", "status": "PENDING", "amount": 10000, "campoNovo": "x" },
                  "error": null,
                  "success": true
                }
                """;

        ApiResponse<Bill> resposta =
                mapper.readValue(json, new TypeReference<ApiResponse<Bill>>() {});

        assertTrue(resposta.success());
        assertEquals("bill_abc123xyz", resposta.data().id());
        assertEquals(10000, resposta.data().amount());
        assertNull(resposta.error());
    }

    @Test
    void leRespostaDeErroSemSuccess() throws Exception {
        String json = """
                { "error": "Invalid or inactive API key" }
                """;

        ApiResponse<Bill> resposta =
                mapper.readValue(json, new TypeReference<ApiResponse<Bill>>() {});

        assertFalse(resposta.success());
        assertNull(resposta.data());
        assertEquals("Invalid or inactive API key", resposta.error());
    }
}
