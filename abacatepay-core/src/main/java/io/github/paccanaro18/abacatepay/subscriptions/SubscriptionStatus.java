package io.github.paccanaro18.abacatepay.subscriptions;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum SubscriptionStatus {
    PENDING, ACTIVE, CANCELLED,

    @JsonEnumDefaultValue
    UNKNOWN
}
