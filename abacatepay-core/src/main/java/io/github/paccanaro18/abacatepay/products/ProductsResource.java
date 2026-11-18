package io.github.paccanaro18.abacatepay.products;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.paccanaro18.abacatepay.ApiResponse;
import io.github.paccanaro18.abacatepay.Page;
import io.github.paccanaro18.abacatepay.PagedApiResponse;
import io.github.paccanaro18.abacatepay.http.AbacatePayHttpClient;
import io.github.paccanaro18.abacatepay.http.QueryParams;

import java.util.Objects;

public final class ProductsResource {

    private static final TypeReference<ApiResponse<Product>> PRODUCT = new TypeReference<>() {};
    private static final TypeReference<PagedApiResponse<Product>> PRODUCT_PAGE = new TypeReference<>() {};

    private final AbacatePayHttpClient http;

    public ProductsResource(AbacatePayHttpClient http) {
        this.http = Objects.requireNonNull(http, "http");
    }

    public Product create(CreateProductRequest request) {
        Objects.requireNonNull(request, "request");
        return http.post("/products/create", request, PRODUCT);
    }

    public Product get(String id) {
        return getBy("id", id);
    }

    public Product getByExternalId(String externalId) {
        return getBy("externalId", externalId);
    }

    public Page<Product> list() {
        return list(ListProductsRequest.builder().build());
    }

    public Page<Product> list(ListProductsRequest request) {
        Objects.requireNonNull(request, "request");
        return http.getPage("/products/list" + request.toQuery(), PRODUCT_PAGE);
    }

    public Product delete(String id) {
        requireText(id, "id");
        return http.post("/products/delete" + new QueryParams().add("id", id).build(), null, PRODUCT);
    }

    private Product getBy(String param, String value) {
        requireText(value, param);
        return http.get("/products/get" + new QueryParams().add(param, value).build(), PRODUCT);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " é obrigatório");
        }
    }
}
