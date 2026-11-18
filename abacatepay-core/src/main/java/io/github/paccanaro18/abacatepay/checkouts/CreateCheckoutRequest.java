package io.github.paccanaro18.abacatepay.checkouts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record CreateCheckoutRequest(
        List<CheckoutItem> items,
        List<PaymentMethod> methods,
        String externalId,
        String customerId,
        String returnUrl,
        String completionUrl,
        List<String> coupons,
        Map<String, Object> metadata) {

    public CreateCheckoutRequest {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A cobrança precisa de pelo menos um item");
        }
        for (CheckoutItem item : items) {
            if (item.id() == null || item.id().isBlank()) {
                throw new IllegalArgumentException("O item precisa de um id de produto");
            }
            if (item.quantity() < 1) {
                throw new IllegalArgumentException("A quantidade do item deve ser no mínimo 1");
            }
        }
        if (methods != null && methods.isEmpty()) {
            throw new IllegalArgumentException("Se informar methods, use pelo menos um método");
        }
        if (methods != null && methods.contains(PaymentMethod.UNKNOWN)) {
            throw new IllegalArgumentException("PaymentMethod.UNKNOWN não pode ser enviado à API");
        }
        items = List.copyOf(items);
        methods = methods == null ? null : List.copyOf(methods);
        coupons = coupons == null ? null : List.copyOf(coupons);
        metadata = metadata == null ? null : Map.copyOf(metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<CheckoutItem> items = new ArrayList<>();
        private List<PaymentMethod> methods;
        private String externalId;
        private String customerId;
        private String returnUrl;
        private String completionUrl;
        private List<String> coupons;
        private Map<String, Object> metadata;

        public Builder item(String productId, int quantity) {
            items.add(new CheckoutItem(productId, quantity));
            return this;
        }

        public Builder methods(PaymentMethod... methods) {
            this.methods = List.of(methods);
            return this;
        }

        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder returnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public Builder completionUrl(String completionUrl) {
            this.completionUrl = completionUrl;
            return this;
        }

        public Builder coupons(String... coupons) {
            this.coupons = List.of(coupons);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public CreateCheckoutRequest build() {
            return new CreateCheckoutRequest(items, methods, externalId, customerId,
                    returnUrl, completionUrl, coupons, metadata);
        }
    }
}