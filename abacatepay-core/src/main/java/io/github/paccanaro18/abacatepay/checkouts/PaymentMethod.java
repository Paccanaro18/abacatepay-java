package io.github.paccanaro18.abacatepay.checkouts;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PaymentMethod {
    PIX, CARD, BOLETO,

    @JsonEnumDefaultValue
    UNKNOWN
}
