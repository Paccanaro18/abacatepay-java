package io.github.paccanaro18.abacatepay.subscriptions;

public record RetryPolicy(Integer maxRetry, Integer retryEvery) {
}
