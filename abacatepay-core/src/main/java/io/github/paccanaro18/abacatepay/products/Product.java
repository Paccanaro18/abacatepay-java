package io.github.paccanaro18.abacatepay.products;

import java.time.Instant;

public record Product(
        String id,
        String externalId,
        String name,
        String description,
        String imageUrl,
        long price,
        String currency,
        Boolean devMode,
        ProductStatus status,
        BillingCycle cycle,
        Boolean hasFile,
        Instant createdAt,
        Instant updatedAt) {
}
