package io.github.paccanaro18.abacatepay;

import io.github.paccanaro18.abacatepay.checkouts.CheckoutsResource;
import io.github.paccanaro18.abacatepay.http.AbacatePayHttpClient;

import java.util.Objects;

public final class AbacatePay {

    public static final String DEFAULT_BASE_URL = "https://api.abacatepay.com/v2";

    private final CheckoutsResource checkouts;

    public AbacatePay(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }

    public AbacatePay(String apiKey, String baseUrl) {
        Objects.requireNonNull(apiKey, "apiKey");
        AbacatePayHttpClient http = new AbacatePayHttpClient(apiKey, baseUrl);
        this.checkouts = new CheckoutsResource(http);
    }

    public CheckoutsResource checkouts() {
        return checkouts;
    }
}