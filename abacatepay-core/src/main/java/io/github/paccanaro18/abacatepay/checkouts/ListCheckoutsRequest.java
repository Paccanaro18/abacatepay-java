package io.github.paccanaro18.abacatepay.checkouts;

import io.github.paccanaro18.abacatepay.http.QueryParams;

import java.time.LocalDate;

public record ListCheckoutsRequest(
        Integer limit,
        String after,
        String before,
        LocalDate startDate,
        LocalDate endDate,
        String id,
        String externalId,
        CheckoutStatus status,
        PaymentMethod method,
        String customerId,
        String keyword) {

    private static final int MAX_LIMIT = 100;

    public ListCheckoutsRequest {
        if (limit != null && (limit < 1 || limit > MAX_LIMIT)) {
            throw new IllegalArgumentException("limit deve ficar entre 1 e " + MAX_LIMIT);
        }
        if (status == CheckoutStatus.UNKNOWN) {
            throw new IllegalArgumentException("CheckoutStatus.UNKNOWN não pode ser usado como filtro");
        }
        if (method == PaymentMethod.UNKNOWN) {
            throw new IllegalArgumentException("PaymentMethod.UNKNOWN não pode ser usado como filtro");
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
                .add("id", id)
                .add("externalId", externalId)
                .add("status", status)
                .add("method", method)
                .add("customerId", customerId)
                .add("keyword", keyword)
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
        private String id;
        private String externalId;
        private CheckoutStatus status;
        private PaymentMethod method;
        private String customerId;
        private String keyword;

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

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder status(CheckoutStatus status) {
            this.status = status;
            return this;
        }

        public Builder method(PaymentMethod method) {
            this.method = method;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public ListCheckoutsRequest build() {
            return new ListCheckoutsRequest(limit, after, before, startDate, endDate,
                    id, externalId, status, method, customerId, keyword);
        }
    }
}
