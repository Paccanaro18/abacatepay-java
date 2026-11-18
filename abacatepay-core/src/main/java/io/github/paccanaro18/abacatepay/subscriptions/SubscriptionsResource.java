package io.github.paccanaro18.abacatepay.subscriptions;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.paccanaro18.abacatepay.ApiResponse;
import io.github.paccanaro18.abacatepay.Page;
import io.github.paccanaro18.abacatepay.PagedApiResponse;
import io.github.paccanaro18.abacatepay.checkouts.Checkout;
import io.github.paccanaro18.abacatepay.http.AbacatePayHttpClient;
import io.github.paccanaro18.abacatepay.http.QueryParams;

import java.util.Map;
import java.util.Objects;

public final class SubscriptionsResource {

    private static final TypeReference<ApiResponse<Checkout>> CHECKOUT = new TypeReference<>() {};
    private static final TypeReference<ApiResponse<Subscription>> SUBSCRIPTION = new TypeReference<>() {};
    private static final TypeReference<PagedApiResponse<Subscription>> SUBSCRIPTION_PAGE = new TypeReference<>() {};

    private final AbacatePayHttpClient http;

    public SubscriptionsResource(AbacatePayHttpClient http) {
        this.http = Objects.requireNonNull(http, "http");
    }

    public Checkout createCheckout(CreateSubscriptionCheckoutRequest request) {
        Objects.requireNonNull(request, "request");
        return http.post("/subscriptions/create", request, CHECKOUT);
    }

    public Subscription get(String id) {
        return getBy("id", id);
    }

    public Subscription getByExternalId(String externalId) {
        return getBy("externalId", externalId);
    }

    public Subscription getByCustomerId(String customerId) {
        return getBy("customerId", customerId);
    }

    public Page<Subscription> list() {
        return list(ListSubscriptionsRequest.builder().build());
    }

    public Page<Subscription> list(ListSubscriptionsRequest request) {
        Objects.requireNonNull(request, "request");
        return http.getPage("/subscriptions/list" + request.toQuery(), SUBSCRIPTION_PAGE);
    }

    public Subscription cancel(String id) {
        requireText(id, "id");
        return http.post("/subscriptions/cancel", Map.of("id", id), SUBSCRIPTION);
    }

    public Checkout deleteCheckout(String checkoutId) {
        requireText(checkoutId, "checkoutId");
        return http.post("/subscriptions/checkouts/delete" + new QueryParams().add("id", checkoutId).build(),
                null, CHECKOUT);
    }

    private Subscription getBy(String param, String value) {
        requireText(value, param);
        return http.get("/subscriptions/get" + new QueryParams().add(param, value).build(), SUBSCRIPTION);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " é obrigatório");
        }
    }
}
