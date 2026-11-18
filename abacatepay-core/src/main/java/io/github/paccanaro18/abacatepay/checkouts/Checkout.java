package io.github.paccanaro18.abacatepay.checkouts;

import java.util.List;
import java.util.Map;

public record Checkout(
        String id,
        String externalId,
        String url,
        long amount,
        Long paidAmount,
        Long platformFee,
        String frequency,
        CheckoutStatus status,
        Boolean devMode,
        List<PaymentMethod> methods,
        Integer installmentsCount,
        List<CheckoutItem> items,
        String customerId,
        String receiptUrl,
        Map<String, Object> metadata,
        List<String> coupons,
        String dueDate,
        String createdAt,
        String updatedAt) {
}