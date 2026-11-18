package io.github.paccanaro18.abacatepay.subscriptions;

import io.github.paccanaro18.abacatepay.checkouts.PaymentMethod;

import java.time.Instant;
import java.util.List;

public record Subscription(
        String id,
        String checkoutId,
        String customerId,
        long amount,
        SubscriptionStatus status,
        PaymentMethod method,
        List<String> coupons,
        Boolean devMode,
        Integer trialDays,
        Instant trialEndsAt,
        RetryPolicy retryPolicy,
        Instant createdAt,
        Instant updatedAt) {
}
