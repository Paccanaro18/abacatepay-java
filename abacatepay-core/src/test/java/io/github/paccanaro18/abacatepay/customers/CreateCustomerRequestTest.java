package io.github.paccanaro18.abacatepay.customers;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreateCustomerRequestTest {

    private static Address enderecoValido() {
        return new Address("01310100", "Avenida Paulista", "1000", "Bela Vista", "São Paulo", "SP", null);
    }

    @Test
    void soOEmailEObrigatorio() {
        CreateCustomerRequest request = CreateCustomerRequest.builder().email("joao@exemplo.com").build();

        assertEquals("joao@exemplo.com", request.email());
    }

    @Test
    void camposNulosNaoSaoEnviadosParaAApi() throws Exception {
        CreateCustomerRequest request = CreateCustomerRequest.builder().email("joao@exemplo.com").build();

        JsonNode json = AbacatePayJson.mapper().readTree(AbacatePayJson.mapper().writeValueAsString(request));

        assertEquals("joao@exemplo.com", json.get("email").asText());
        assertFalse(json.has("name"));
        assertFalse(json.has("address"));
        assertFalse(json.has("metadata"));
    }

    @Test
    void aceitaTodosOsCampos() {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .email("joao@exemplo.com")
                .name("João Silva")
                .taxId("11144477735")
                .cellphone("11999999999")
                .zipCode("01310100")
                .address(enderecoValido())
                .metadata(Map.of("plano", "premium"))
                .build();

        assertEquals("João Silva", request.name());
        assertEquals("Avenida Paulista", request.address().street());
        assertEquals("premium", request.metadata().get("plano"));
    }

    @Test
    void emailAusenteOuInvalidoEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> CreateCustomerRequest.builder().build());
        assertThrows(IllegalArgumentException.class, () -> CreateCustomerRequest.builder().email(" ").build());
        assertThrows(IllegalArgumentException.class,
                () -> CreateCustomerRequest.builder().email("sem-arroba").build());
    }

    @Test
    void cepPrecisaTerOitoDigitos() {
        assertThrows(IllegalArgumentException.class, () -> CreateCustomerRequest.builder()
                .email("joao@exemplo.com").zipCode("01310-100").build());
    }

    @Test
    void enderecoPrecisaDosCamposObrigatorios() {
        Address semRua = new Address("01310100", null, "1000", "Bela Vista", "São Paulo", "SP", null);

        assertThrows(IllegalArgumentException.class, () -> CreateCustomerRequest.builder()
                .email("joao@exemplo.com").address(semRua).build());
    }

    @Test
    void estadoDoEnderecoTemDuasLetras() {
        Address estadoLongo = new Address("01310100", "Avenida Paulista", "1000", "Bela Vista", "São Paulo", "SAO", null);

        assertThrows(IllegalArgumentException.class, () -> CreateCustomerRequest.builder()
                .email("joao@exemplo.com").address(estadoLongo).build());
    }
}
