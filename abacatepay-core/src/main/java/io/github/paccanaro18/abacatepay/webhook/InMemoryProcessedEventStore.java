package io.github.paccanaro18.abacatepay.webhook;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class InMemoryProcessedEventStore implements ProcessedEventStore {

    private static final int DEFAULT_MAX_SIZE = 10_000;

    private final Set<String> ids;

    public InMemoryProcessedEventStore() {
        this(DEFAULT_MAX_SIZE);
    }

    public InMemoryProcessedEventStore(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize deve ser pelo menos 1");
        }
        this.ids = Collections.newSetFromMap(Collections.synchronizedMap(
                new LinkedHashMap<String, Boolean>() {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                        return size() > maxSize;
                    }
                }));
    }

    @Override
    public boolean alreadyProcessed(String eventId) {
        return ids.contains(eventId);
    }

    @Override
    public void markProcessed(String eventId) {
        ids.add(eventId);
    }
}
