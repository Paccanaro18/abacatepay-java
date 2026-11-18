package io.github.paccanaro18.abacatepay.products;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum BillingCycle {
    WEEKLY, MONTHLY, QUARTERLY, SEMIANNUALLY, ANNUALLY,

    @JsonEnumDefaultValue
    UNKNOWN
}
