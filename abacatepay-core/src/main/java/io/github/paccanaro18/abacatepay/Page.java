package io.github.paccanaro18.abacatepay;

import java.util.List;

public record Page<T>(List<T> items, boolean hasMore, String next, String before) {

    public Page {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
