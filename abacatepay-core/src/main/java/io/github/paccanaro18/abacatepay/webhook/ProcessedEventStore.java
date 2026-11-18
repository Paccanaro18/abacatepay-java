package io.github.paccanaro18.abacatepay.webhook;

public interface ProcessedEventStore {

    boolean alreadyProcessed(String eventId);

    void markProcessed(String eventId);
}
