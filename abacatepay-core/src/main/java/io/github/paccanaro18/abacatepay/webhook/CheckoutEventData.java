package io.github.paccanaro18.abacatepay.webhook;

import io.github.paccanaro18.abacatepay.checkouts.Checkout;
import io.github.paccanaro18.abacatepay.customers.Customer;

public record CheckoutEventData(
        Checkout checkout,
        Customer customer,
        PayerInformation payerInformation,
        String reason) {
}
