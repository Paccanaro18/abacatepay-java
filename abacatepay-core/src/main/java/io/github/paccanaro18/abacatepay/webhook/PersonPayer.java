package io.github.paccanaro18.abacatepay.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PersonPayer(
        String name,
        String taxId,
        @JsonProperty("isSameAsCustomer") boolean isSameAsCustomer) {
}