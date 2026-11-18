package io.github.paccanaro18.abacatepay.checkouts;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.paccanaro18.abacatepay.ApiResponse;
import io.github.paccanaro18.abacatepay.http.AbacatePayHttpClient;

import java.util.Objects;

public final class CheckoutsResource {

    private static final TypeReference<ApiResponse<Checkout>> CHECKOUT = new TypeReference<>() {};

    private final AbacatePayHttpClient http;

    public CheckoutsResource(AbacatePayHttpClient http) {
        this.http = Objects.requireNonNull(http, "http");
    }

    public Checkout create(CreateCheckoutRequest request) {
        Objects.requireNonNull(request, "request");
        return http.post("/checkouts/create", request, CHECKOUT);
    }
}