package io.github.paccanaro18.abacatepay.products;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ProductStatus {
    ACTIVE, INACTIVE, ARCHIVED,

    @JsonEnumDefaultValue
    UNKNOWN
}
