package io.github.paccanaro18.abacatepay.checkouts;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListCheckoutsRequestTest {

    @Test
    void semFiltrosGeraQueryVazia() {
        assertEquals("", ListCheckoutsRequest.builder().build().toQuery());
    }

    @Test
    void geraAQueryNaOrdemDocumentada() {
        ListCheckoutsRequest request = ListCheckoutsRequest.builder()
                .limit(20)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 31))
                .status(CheckoutStatus.PAID)
                .method(PaymentMethod.PIX)
                .keyword("camiseta azul")
                .build();

        assertEquals("?limit=20&startDate=2026-01-01&endDate=2026-01-31"
                + "&status=PAID&method=PIX&keyword=camiseta+azul", request.toQuery());
    }

    @Test
    void cursoresViramAfterEBefore() {
        ListCheckoutsRequest request = ListCheckoutsRequest.builder()
                .after("cur_2")
                .before("cur_0")
                .build();

        assertEquals("?after=cur_2&before=cur_0", request.toQuery());
    }

    @Test
    void limiteForaDaFaixaEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> ListCheckoutsRequest.builder().limit(0).build());
        assertThrows(IllegalArgumentException.class, () -> ListCheckoutsRequest.builder().limit(101).build());
    }

    @Test
    void statusEMetodoUnknownNaoPodemSerEnviados() {
        assertThrows(IllegalArgumentException.class,
                () -> ListCheckoutsRequest.builder().status(CheckoutStatus.UNKNOWN).build());
        assertThrows(IllegalArgumentException.class,
                () -> ListCheckoutsRequest.builder().method(PaymentMethod.UNKNOWN).build());
    }

    @Test
    void dataInicialDepoisDaFinalEhRejeitada() {
        assertThrows(IllegalArgumentException.class, () -> ListCheckoutsRequest.builder()
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .build());
    }
}
