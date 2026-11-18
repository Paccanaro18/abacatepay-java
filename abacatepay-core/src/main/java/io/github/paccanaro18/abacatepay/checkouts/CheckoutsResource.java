package io.github.paccanaro18.abacatepay.checkouts;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.paccanaro18.abacatepay.ApiResponse;
import io.github.paccanaro18.abacatepay.Page;
import io.github.paccanaro18.abacatepay.PagedApiResponse;
import io.github.paccanaro18.abacatepay.http.AbacatePayHttpClient;
import io.github.paccanaro18.abacatepay.http.QueryParams;

import java.util.Objects;

public final class CheckoutsResource {

    private static final TypeReference<ApiResponse<Checkout>> CHECKOUT = new TypeReference<>() {};
    private static final TypeReference<PagedApiResponse<Checkout>> CHECKOUT_PAGE = new TypeReference<>() {};

    private final AbacatePayHttpClient http;

    public CheckoutsResource(AbacatePayHttpClient http) {
        this.http = Objects.requireNonNull(http, "http");
    }

    public Checkout create(CreateCheckoutRequest request) {
        Objects.requireNonNull(request, "request");
        return http.post("/checkouts/create", request, CHECKOUT);
    }

    public Checkout get(String id) {
        requireText(id, "id");
        return http.get("/checkouts/get" + new QueryParams().add("id", id).build(), CHECKOUT);
    }

    public Checkout getByExternalId(String externalId) {
        requireText(externalId, "externalId");
        return http.get("/checkouts/get" + new QueryParams().add("externalId", externalId).build(), CHECKOUT);
    }

    public Page<Checkout> list() {
        return list(ListCheckoutsRequest.builder().build());
    }

    public Page<Checkout> list(ListCheckoutsRequest request) {
        Objects.requireNonNull(request, "request");
        return http.getPage("/checkouts/list" + request.toQuery(), CHECKOUT_PAGE);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " é obrigatório");
        }
    }
}
