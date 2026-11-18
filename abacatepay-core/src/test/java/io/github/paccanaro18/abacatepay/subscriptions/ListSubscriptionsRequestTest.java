package io.github.paccanaro18.abacatepay.subscriptions;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListSubscriptionsRequestTest {

    @Test
    void semFiltrosGeraQueryVazia() {
        assertEquals("", ListSubscriptionsRequest.builder().build().toQuery());
    }

    @Test
    void geraAQueryNaOrdemDocumentada() {
        ListSubscriptionsRequest request = ListSubscriptionsRequest.builder()
                .limit(20)
                .after("cur_2")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 31))
                .status(SubscriptionStatus.ACTIVE)
                .checkoutId("bill_abc123xyz")
                .build();

        assertEquals("?limit=20&after=cur_2&startDate=2026-01-01&endDate=2026-01-31"
                + "&status=ACTIVE&checkoutId=bill_abc123xyz", request.toQuery());
    }

    @Test
    void limiteForaDaFaixaEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> ListSubscriptionsRequest.builder().limit(0).build());
        assertThrows(IllegalArgumentException.class, () -> ListSubscriptionsRequest.builder().limit(101).build());
    }

    @Test
    void filtroDeStatusAceitaSoAtivaECancelada() {
        assertThrows(IllegalArgumentException.class,
                () -> ListSubscriptionsRequest.builder().status(SubscriptionStatus.PENDING).build());
        assertThrows(IllegalArgumentException.class,
                () -> ListSubscriptionsRequest.builder().status(SubscriptionStatus.UNKNOWN).build());
    }

    @Test
    void dataInicialDepoisDaFinalEhRejeitada() {
        assertThrows(IllegalArgumentException.class, () -> ListSubscriptionsRequest.builder()
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .build());
    }
}
