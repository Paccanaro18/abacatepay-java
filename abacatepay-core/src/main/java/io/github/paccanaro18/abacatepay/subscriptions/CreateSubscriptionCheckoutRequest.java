package io.github.paccanaro18.abacatepay.subscriptions;

import io.github.paccanaro18.abacatepay.checkouts.CheckoutItem;
import io.github.paccanaro18.abacatepay.checkouts.PaymentMethod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record CreateSubscriptionCheckoutRequest(
        List<CheckoutItem> items,
        List<PaymentMethod> methods,
        String externalId,
        String customerId,
        String returnUrl,
        String completionUrl,
        List<String> coupons,
        Map<String, Object> metadata,
        CardOptions card,
        RetryPolicy retryPolicy,
        String upSellProductId,
        LocalDate dueDate,
        BoletoInterest interest,
        BoletoFine fine) {

    private static final int MAX_COUPONS = 50;
    private static final int MAX_DUE_DAYS = 365;

    public CreateSubscriptionCheckoutRequest {
        if (items == null || items.size() != 1) {
            throw new IllegalArgumentException("O checkout de assinatura precisa de exatamente um item");
        }
        CheckoutItem item = items.get(0);
        if (item.id() == null || item.id().isBlank()) {
            throw new IllegalArgumentException("O item precisa de um id de produto");
        }
        if (item.quantity() < 1) {
            throw new IllegalArgumentException("A quantidade do item deve ser no mínimo 1");
        }
        if (methods != null) {
            if (methods.isEmpty()) {
                throw new IllegalArgumentException("Se informar methods, use pelo menos um método");
            }
            for (PaymentMethod method : methods) {
                if (method == PaymentMethod.UNKNOWN) {
                    throw new IllegalArgumentException("PaymentMethod.UNKNOWN não pode ser enviado à API");
                }
            }
        }
        if (card != null) {
            if (card.maxInstallments() < 1 || card.maxInstallments() > 12) {
                throw new IllegalArgumentException("maxInstallments deve ficar entre 1 e 12");
            }
            if (methods != null && !methods.contains(PaymentMethod.CARD)) {
                throw new IllegalArgumentException("As opções de cartão exigem CARD em methods");
            }
        }
        if (retryPolicy != null) {
            if (retryPolicy.maxRetry() != null && (retryPolicy.maxRetry() < 1 || retryPolicy.maxRetry() > 10)) {
                throw new IllegalArgumentException("maxRetry deve ficar entre 1 e 10");
            }
            if (retryPolicy.retryEvery() != null
                    && (retryPolicy.retryEvery() < 1 || retryPolicy.retryEvery() > 30)) {
                throw new IllegalArgumentException("retryEvery deve ficar entre 1 e 30");
            }
        }
        if (upSellProductId != null && upSellProductId.isBlank()) {
            throw new IllegalArgumentException("upSellProductId não pode ser vazio");
        }
        if (dueDate != null || interest != null || fine != null) {
            if (methods == null || !methods.contains(PaymentMethod.BOLETO)) {
                throw new IllegalArgumentException("Vencimento, juros e multa exigem BOLETO em methods");
            }
        }
        if (dueDate != null) {
            LocalDate hoje = LocalDate.now();
            if (dueDate.isBefore(hoje)) {
                throw new IllegalArgumentException("dueDate não pode estar no passado");
            }
            if (dueDate.isAfter(hoje.plusDays(MAX_DUE_DAYS))) {
                throw new IllegalArgumentException("dueDate não pode passar de " + MAX_DUE_DAYS + " dias");
            }
        }
        if (interest != null && interest.value() < 0) {
            throw new IllegalArgumentException("Os juros não podem ser negativos");
        }
        if (fine != null && (fine.value() < 0 || fine.type() == null)) {
            throw new IllegalArgumentException("A multa precisa de valor não negativo e de tipo");
        }
        if (coupons != null && coupons.size() > MAX_COUPONS) {
            throw new IllegalArgumentException("No máximo " + MAX_COUPONS + " cupons");
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
        private CardOptions card;
        private RetryPolicy retryPolicy;
        private String upSellProductId;
        private LocalDate dueDate;
        private BoletoInterest interest;
        private BoletoFine fine;

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

        public Builder card(int maxInstallments, boolean passFeesToCustomer) {
            this.card = new CardOptions(maxInstallments, passFeesToCustomer);
            return this;
        }

        public Builder retryPolicy(int maxRetry, int retryEvery) {
            this.retryPolicy = new RetryPolicy(maxRetry, retryEvery);
            return this;
        }

        public Builder upSellProductId(String upSellProductId) {
            this.upSellProductId = upSellProductId;
            return this;
        }

        public Builder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder interest(int value) {
            this.interest = new BoletoInterest(value);
            return this;
        }

        public Builder fine(int value, FineType type) {
            this.fine = new BoletoFine(value, type);
            return this;
        }

        public CreateSubscriptionCheckoutRequest build() {
            return new CreateSubscriptionCheckoutRequest(items, methods, externalId, customerId,
                    returnUrl, completionUrl, coupons, metadata, card, retryPolicy,
                    upSellProductId, dueDate, interest, fine);
        }
    }
}
