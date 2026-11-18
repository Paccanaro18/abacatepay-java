package io.github.paccanaro18.abacatepay.webhook;

public interface ProcessedEventStore {

    boolean alreadyProcessed(String eventId);

    void markProcessed(String eventId);

    /**
     * Reivindica o evento de forma atômica: devolve {@code true} apenas para o
     * primeiro chamador. Quem recebe {@code false} deve ignorar o evento.
     */
    boolean tryClaim(String eventId);

    /** Desfaz uma reivindicação, para que a reentrega do evento seja processada. */
    void release(String eventId);
}
