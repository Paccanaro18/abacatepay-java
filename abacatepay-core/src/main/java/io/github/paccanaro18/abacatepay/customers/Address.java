package io.github.paccanaro18.abacatepay.customers;

public record Address(
        String zipCode,
        String street,
        String number,
        String neighborhood,
        String city,
        String state,
        String complement) {
}
