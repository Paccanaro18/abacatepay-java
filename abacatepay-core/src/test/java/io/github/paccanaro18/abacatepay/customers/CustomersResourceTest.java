package io.github.paccanaro18.abacatepay.customers;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.paccanaro18.abacatepay.AbacatePay;
import io.github.paccanaro18.abacatepay.AbacatePayJson;
import io.github.paccanaro18.abacatepay.FakeApi;
import io.github.paccanaro18.abacatepay.Page;
import io.github.paccanaro18.abacatepay.exception.AbacatePayNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomersResourceTest {

    private static final String CLIENTE_JSON = """
            {
              "data": {
                "id": "cust_aebxkhDZNaMmJeKsy0AHS0FQ",
                "devMode": true,
                "name": "Daniel Lima",
                "cellphone": "(11) 4002-8922",
                "email": "daniel_lima@abacatepay.com",
                "taxId": "11144477735",
                "country": "BR",
                "zipCode": "01310-100",
                "address": {
                  "zipCode": "01310-100",
                  "street": "Avenida Paulista",
                  "number": "1000",
                  "neighborhood": "Bela Vista",
                  "city": "São Paulo",
                  "state": "SP",
                  "complement": "Sala 12"
                },
                "metadata": { "source": "landing-page" }
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
    void criaClienteEEnviaOsCamposInformados() throws Exception {
        api.respond("/customers/create", 200, CLIENTE_JSON);

        Customer cliente = abacate.customers().create(CreateCustomerRequest.builder()
                .email("daniel_lima@abacatepay.com")
                .name("Daniel Lima")
                .build());

        assertEquals("POST", api.lastMethod());
        assertEquals("/customers/create", api.lastUri());
        JsonNode enviado = AbacatePayJson.mapper().readTree(api.lastBody());
        assertEquals("daniel_lima@abacatepay.com", enviado.get("email").asText());
        assertEquals("Daniel Lima", enviado.get("name").asText());
        assertFalse(enviado.has("taxId"));

        assertEquals("cust_aebxkhDZNaMmJeKsy0AHS0FQ", cliente.id());
        assertTrue(cliente.devMode());
        assertEquals("(11) 4002-8922", cliente.cellphone());
        assertEquals("BR", cliente.country());
        assertEquals("Avenida Paulista", cliente.address().street());
        assertEquals("Sala 12", cliente.address().complement());
        assertEquals("landing-page", cliente.metadata().get("source"));
    }

    @Test
    void buscaPorId() {
        api.respond("/customers/get", 200, CLIENTE_JSON);

        Customer cliente = abacate.customers().get("cust_aebxkhDZNaMmJeKsy0AHS0FQ");

        assertEquals("GET", api.lastMethod());
        assertEquals("/customers/get?id=cust_aebxkhDZNaMmJeKsy0AHS0FQ", api.lastUri());
        assertEquals("Daniel Lima", cliente.name());
    }

    @Test
    void buscaPorEmailEPorCpf() {
        api.respond("/customers/get", 200, CLIENTE_JSON);

        abacate.customers().getByEmail("daniel_lima@abacatepay.com");
        assertEquals("/customers/get?email=daniel_lima%40abacatepay.com", api.lastUri());

        abacate.customers().getByTaxId("111.444.777-35");
        assertEquals("/customers/get?taxId=111.444.777-35", api.lastUri());
    }

    @Test
    void clienteInexistenteLancaNotFound() {
        api.respond("/customers/get", 404, """
                { "success": false, "data": null, "error": "Customer not found" }
                """);

        AbacatePayNotFoundException ex = assertThrows(
                AbacatePayNotFoundException.class,
                () -> abacate.customers().get("cust_naoexiste"));

        assertEquals("Customer not found", ex.getMessage());
    }

    @Test
    void identificadorEmBrancoFalhaAntesDeChamarAApi() {
        assertThrows(IllegalArgumentException.class, () -> abacate.customers().get(" "));
        assertThrows(IllegalArgumentException.class, () -> abacate.customers().getByEmail(null));
        assertThrows(IllegalArgumentException.class, () -> abacate.customers().getByTaxId(""));
        assertThrows(IllegalArgumentException.class, () -> abacate.customers().delete(null));
    }

    @Test
    void listaDevolvePaginaComCursor() {
        api.respond("/customers/list", 200, """
                {
                  "data": [
                    { "id": "cust_1", "devMode": true, "name": "Ana", "cellphone": "11999999999",
                      "email": "ana@exemplo.com", "taxId": "11144477735" }
                  ],
                  "success": true,
                  "error": null,
                  "pagination": { "hasMore": true, "next": "cur_2", "before": null }
                }
                """);

        Page<Customer> pagina = abacate.customers().list(
                ListCustomersRequest.builder().limit(1).email("ana@exemplo.com").build());

        assertEquals("/customers/list?limit=1&email=ana%40exemplo.com", api.lastUri());
        assertEquals(1, pagina.items().size());
        assertEquals("Ana", pagina.items().get(0).name());
        assertTrue(pagina.hasMore());
        assertEquals("cur_2", pagina.next());
    }

    @Test
    void excluiClienteComPostSemCorpoEIdNaQuery() {
        api.respond("/customers/delete", 200, CLIENTE_JSON);

        Customer excluido = abacate.customers().delete("cust_aebxkhDZNaMmJeKsy0AHS0FQ");

        assertEquals("POST", api.lastMethod());
        assertEquals("/customers/delete?id=cust_aebxkhDZNaMmJeKsy0AHS0FQ", api.lastUri());
        assertEquals("", api.lastBody());
        assertEquals("cust_aebxkhDZNaMmJeKsy0AHS0FQ", excluido.id());
    }
}
