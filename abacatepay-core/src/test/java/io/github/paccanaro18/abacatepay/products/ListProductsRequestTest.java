package io.github.paccanaro18.abacatepay.products;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListProductsRequestTest {

    @Test
    void semFiltrosGeraQueryVazia() {
        assertEquals("", ListProductsRequest.builder().build().toQuery());
    }

    @Test
    void geraAQueryNaOrdemDocumentada() {
        ListProductsRequest request = ListProductsRequest.builder()
                .limit(20)
                .endDate(LocalDate.of(2026, 1, 31))
                .externalId("prod-123")
                .status(ProductStatus.ACTIVE)
                .keyword("camiseta azul")
                .currency("BRL")
                .build();

        assertEquals("?limit=20&endDate=2026-01-31&externalId=prod-123"
                + "&status=ACTIVE&keyword=camiseta+azul&currency=BRL", request.toQuery());
    }

    @Test
    void limiteEStatusInvalidosSaoRejeitados() {
        assertThrows(IllegalArgumentException.class, () -> ListProductsRequest.builder().limit(101).build());
        assertThrows(IllegalArgumentException.class,
                () -> ListProductsRequest.builder().status(ProductStatus.UNKNOWN).build());
        assertThrows(IllegalArgumentException.class,
                () -> ListProductsRequest.builder().status(ProductStatus.ARCHIVED).build());
    }
}
