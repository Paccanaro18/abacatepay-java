package io.github.paccanaro18.abacatepay.customers;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.paccanaro18.abacatepay.ApiResponse;
import io.github.paccanaro18.abacatepay.Page;
import io.github.paccanaro18.abacatepay.PagedApiResponse;
import io.github.paccanaro18.abacatepay.http.AbacatePayHttpClient;
import io.github.paccanaro18.abacatepay.http.QueryParams;

import java.util.Objects;

public final class CustomersResource {

    private static final TypeReference<ApiResponse<Customer>> CUSTOMER = new TypeReference<>() {};
    private static final TypeReference<PagedApiResponse<Customer>> CUSTOMER_PAGE = new TypeReference<>() {};

    private final AbacatePayHttpClient http;

    public CustomersResource(AbacatePayHttpClient http) {
        this.http = Objects.requireNonNull(http, "http");
    }

    public Customer create(CreateCustomerRequest request) {
        Objects.requireNonNull(request, "request");
        return http.post("/customers/create", request, CUSTOMER);
    }

    public Customer get(String id) {
        return getBy("id", id);
    }

    public Customer getByEmail(String email) {
        return getBy("email", email);
    }

    public Customer getByTaxId(String taxId) {
        return getBy("taxId", taxId);
    }

    public Page<Customer> list() {
        return list(ListCustomersRequest.builder().build());
    }

    public Page<Customer> list(ListCustomersRequest request) {
        Objects.requireNonNull(request, "request");
        return http.getPage("/customers/list" + request.toQuery(), CUSTOMER_PAGE);
    }

    public Customer delete(String id) {
        requireText(id, "id");
        return http.post("/customers/delete" + new QueryParams().add("id", id).build(), null, CUSTOMER);
    }

    private Customer getBy(String param, String value) {
        requireText(value, param);
        return http.get("/customers/get" + new QueryParams().add(param, value).build(), CUSTOMER);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " é obrigatório");
        }
    }
}
