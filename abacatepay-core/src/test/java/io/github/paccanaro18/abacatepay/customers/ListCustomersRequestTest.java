package io.github.paccanaro18.abacatepay.customers;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListCustomersRequestTest {

    @Test
    void semFiltrosGeraQueryVazia() {
        assertEquals("", ListCustomersRequest.builder().build().toQuery());
    }

    @Test
    void geraAQueryNaOrdemDocumentada() {
        ListCustomersRequest request = ListCustomersRequest.builder()
                .limit(10)
                .after("cur_2")
                .startDate(LocalDate.of(2026, 1, 1))
                .email("joao@exemplo.com")
                .name("João Silva")
                .build();

        assertEquals("?limit=10&after=cur_2&startDate=2026-01-01"
                + "&email=joao%40exemplo.com&name=Jo%C3%A3o+Silva", request.toQuery());
    }

    @Test
    void limiteEDatasInvalidosSaoRejeitados() {
        assertThrows(IllegalArgumentException.class, () -> ListCustomersRequest.builder().limit(0).build());
        assertThrows(IllegalArgumentException.class, () -> ListCustomersRequest.builder().limit(101).build());
        assertThrows(IllegalArgumentException.class, () -> ListCustomersRequest.builder()
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .build());
    }
}
