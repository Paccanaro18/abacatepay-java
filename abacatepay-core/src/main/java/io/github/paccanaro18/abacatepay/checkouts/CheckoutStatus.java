package io.github.paccanaro18.abacatepay.checkouts;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum CheckoutStatus {
    PENDING, EXPIRED, CANCELLED, PAID, REFUNDED, REDEEMED,

    @JsonEnumDefaultValue
    UNKNOWN
}