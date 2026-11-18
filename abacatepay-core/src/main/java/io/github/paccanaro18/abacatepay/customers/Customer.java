package io.github.paccanaro18.abacatepay.customers;

import java.util.Map;

public record Customer(
        String id,
        String name,
        String email,
        String taxId,
        Boolean devMode,
        String cellphone,
        String country,
        String zipCode,
        Address address,
        Map<String, String> metadata) {
}
