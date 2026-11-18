package io.github.paccanaro18.abacatepay.products;

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

class ProductsResourceTest {

    private static final String PRODUTO_JSON = """
            {
              "data": {
                "id": "prod_abc123xyz",
                "externalId": "prod-123",
                "name": "Camiseta Premium",
                "description": "Camiseta 100% algodão",
                "imageUrl": null,
                "price": 8990,
                "currency": "BRL",
                "devMode": true,
                "status": "ACTIVE",
                "cycle": null,
                "hasFile": false,
                "createdAt": "2024-11-04T18:38:28.573Z",
                "updatedAt": "2024-11-04T18:38:28.573Z"
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
    void criaProdutoEEnviaPrecoEmCentavosEMoeda() throws Exception {
        api.respond("/products/create", 200, PRODUTO_JSON);

        Product produto = abacate.products().create(CreateProductRequest.builder()
                .externalId("prod-123")
                .name("Camiseta Premium")
                .price(8990)
                .description("Camiseta 100% algodão")
                .build());

        assertEquals("POST", api.lastMethod());
        assertEquals("/products/create", api.lastUri());
        JsonNode enviado = AbacatePayJson.mapper().readTree(api.lastBody());
        assertEquals(8990, enviado.get("price").asLong());
        assertEquals("BRL", enviado.get("currency").asText());
        assertFalse(enviado.has("cycle"));

        assertEquals("prod_abc123xyz", produto.id());
        assertEquals(8990L, produto.price());
        assertEquals(ProductStatus.ACTIVE, produto.status());
        assertNull(produto.cycle());
        assertFalse(produto.hasFile());
    }

    @Test
    void buscaPorIdEPorExternalId() {
        api.respond("/products/get", 200, PRODUTO_JSON);

        abacate.products().get("prod_abc123xyz");
        assertEquals("GET", api.lastMethod());
        assertEquals("/products/get?id=prod_abc123xyz", api.lastUri());

        abacate.products().getByExternalId("prod-123");
        assertEquals("/products/get?externalId=prod-123", api.lastUri());
    }

    @Test
    void produtoInexistenteLancaNotFound() {
        api.respond("/products/get", 404, """
                { "success": false, "data": null, "error": "Produto não encontrado." }
                """);

        AbacatePayNotFoundException ex = assertThrows(
                AbacatePayNotFoundException.class,
                () -> abacate.products().get("prod_naoexiste"));

        assertEquals("Produto não encontrado.", ex.getMessage());
    }

    @Test
    void identificadorEmBrancoFalhaAntesDeChamarAApi() {
        assertThrows(IllegalArgumentException.class, () -> abacate.products().get(" "));
        assertThrows(IllegalArgumentException.class, () -> abacate.products().getByExternalId(null));
        assertThrows(IllegalArgumentException.class, () -> abacate.products().delete(""));
    }

    @Test
    void listaDevolvePaginaECicloDaAssinatura() {
        api.respond("/products/list", 200, """
                {
                  "data": [
                    { "id": "prod_1", "externalId": "plano", "name": "Plano Mensal", "description": "x",
                      "price": 4990, "currency": "BRL", "devMode": true, "status": "ACTIVE",
                      "cycle": "MONTHLY", "hasFile": false }
                  ],
                  "success": true,
                  "error": null,
                  "pagination": { "hasMore": false, "next": null, "before": null }
                }
                """);

        Page<Product> pagina = abacate.products().list(
                ListProductsRequest.builder().status(ProductStatus.ACTIVE).limit(10).build());

        assertEquals("/products/list?limit=10&status=ACTIVE", api.lastUri());
        assertEquals(BillingCycle.MONTHLY, pagina.items().get(0).cycle());
        assertFalse(pagina.hasMore());
    }

    @Test
    void excluiProdutoComPostSemCorpoEIdNaQuery() {
        api.respond("/products/delete", 200, """
                { "success": true, "error": null,
                  "data": { "id": "prod_abc123xyz", "externalId": "prod-123", "name": "Camiseta Premium",
                            "price": 8990, "currency": "BRL", "status": "ARCHIVED" } }
                """);

        Product excluido = abacate.products().delete("prod_abc123xyz");

        assertEquals("POST", api.lastMethod());
        assertEquals("/products/delete?id=prod_abc123xyz", api.lastUri());
        assertEquals("", api.lastBody());
        assertEquals(ProductStatus.ARCHIVED, excluido.status());
    }
}
