package io.github.paccanaro18.abacatepay.webhook;

import org.junit.jupiter.api.Test;

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
}
