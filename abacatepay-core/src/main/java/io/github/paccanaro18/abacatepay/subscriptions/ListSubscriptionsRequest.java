package io.github.paccanaro18.abacatepay.subscriptions;

import io.github.paccanaro18.abacatepay.http.QueryParams;

import java.time.LocalDate;

public record ListSubscriptionsRequest(
        Integer limit,
        String after,
        String before,
        LocalDate startDate,
        LocalDate endDate,
        SubscriptionStatus status,
        String checkoutId) {

    private static final int MAX_LIMIT = 100;

    public ListSubscriptionsRequest {
        if (limit != null && (limit < 1 || limit > MAX_LIMIT)) {
            throw new IllegalArgumentException("limit deve ficar entre 1 e " + MAX_LIMIT);
        }
        if (status != null && status != SubscriptionStatus.ACTIVE && status != SubscriptionStatus.CANCELLED) {
            throw new IllegalArgumentException("O filtro de status aceita apenas ACTIVE e CANCELLED");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate não pode ser depois de endDate");
        }
    }

    public String toQuery() {
        return new QueryParams()
                .add("limit", limit)
                .add("after", after)
                .add("before", before)
                .add("startDate", startDate)
                .add("endDate", endDate)
                .add("status", status)
                .add("checkoutId", checkoutId)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer limit;
        private String after;
        private String before;
        private LocalDate startDate;
        private LocalDate endDate;
        private SubscriptionStatus status;
        private String checkoutId;

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder after(String after) {
            this.after = after;
            return this;
        }

        public Builder before(String before) {
            this.before = before;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder status(SubscriptionStatus status) {
            this.status = status;
            return this;
        }

        public Builder checkoutId(String checkoutId) {
            this.checkoutId = checkoutId;
            return this;
        }

        public ListSubscriptionsRequest build() {
            return new ListSubscriptionsRequest(limit, after, before, startDate, endDate, status, checkoutId);
        }
    }
}
