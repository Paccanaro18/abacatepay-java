package io.github.paccanaro18.abacatepay.webhook;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryProcessedEventStoreTest {

    @Test
    void idNovoAindaNaoFoiProcessado() {
        ProcessedEventStore store = new InMemoryProcessedEventStore();

        assertFalse(store.alreadyProcessed("evt_1"));
    }

    @Test
    void idMarcadoPassaAConstarComoProcessado() {
        ProcessedEventStore store = new InMemoryProcessedEventStore();

        store.markProcessed("evt_1");

        assertTrue(store.alreadyProcessed("evt_1"));
        assertFalse(store.alreadyProcessed("evt_2"));
    }

    @Test
    void alemDoLimiteEsqueceOIdMaisAntigo() {
        ProcessedEventStore store = new InMemoryProcessedEventStore(2);

        store.markProcessed("evt_1");
        store.markProcessed("evt_2");
        store.markProcessed("evt_3");

        assertFalse(store.alreadyProcessed("evt_1"));
        assertTrue(store.alreadyProcessed("evt_2"));
        assertTrue(store.alreadyProcessed("evt_3"));
    }

    @Test
    void limiteMenorQueUmEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> new InMemoryProcessedEventStore(0));
    }

    @Test
    void tryClaimSoDaCertoNaPrimeiraVez() {
        ProcessedEventStore store = new InMemoryProcessedEventStore();

        assertTrue(store.tryClaim("evt_1"));
        assertFalse(store.tryClaim("evt_1"));
        assertTrue(store.alreadyProcessed("evt_1"));
    }

    @Test
    void releaseDevolveOIdParaPoderSerReivindicado() {
        ProcessedEventStore store = new InMemoryProcessedEventStore();

        store.tryClaim("evt_1");
        store.release("evt_1");

        assertFalse(store.alreadyProcessed("evt_1"));
        assertTrue(store.tryClaim("evt_1"));
    }

    @Test
    void reivindicacoesConcorrentesDoMesmoIdTemUmSoVencedor() throws Exception {
        ProcessedEventStore store = new InMemoryProcessedEventStore();
        int threads = 32;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            resultados.add(pool.submit(() -> {
                largada.await();
                return store.tryClaim("evt_1");
            }));
        }
        largada.countDown();

        int vencedores = 0;
        for (Future<Boolean> f : resultados) {
            if (f.get()) {
                vencedores++;
            }
        }
        pool.shutdown();

        assertEquals(1, vencedores);
    }
}
