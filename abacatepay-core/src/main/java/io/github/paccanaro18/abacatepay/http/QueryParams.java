package io.github.paccanaro18.abacatepay.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class QueryParams {

    private final StringBuilder query = new StringBuilder();

    public QueryParams add(String name, Object value) {
        if (value == null) {
            return this;
        }
        String text = value.toString();
        if (text.isBlank()) {
            return this;
        }
        query.append(query.length() == 0 ? '?' : '&')
                .append(name)
                .append('=')
                .append(URLEncoder.encode(text, StandardCharsets.UTF_8));
        return this;
    }

    public String build() {
        return query.toString();
    }
}
