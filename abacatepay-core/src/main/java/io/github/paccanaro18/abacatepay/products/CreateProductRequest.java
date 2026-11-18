package io.github.paccanaro18.abacatepay.products;

public record CreateProductRequest(
        String externalId,
        String name,
        long price,
        String currency,
        String description,
        String imageUrl,
        String fileUrl,
        BillingCycle cycle,
        Integer trialDays) {

    private static final String BRL = "BRL";

    public CreateProductRequest {
        if (blank(externalId)) {
            throw new IllegalArgumentException("O produto precisa de um externalId");
        }
        if (blank(name)) {
            throw new IllegalArgumentException("O produto precisa de um nome");
        }
        if (price < 1) {
            throw new IllegalArgumentException("price deve ser de pelo menos 1 centavo");
        }
        currency = currency == null ? BRL : currency;
        if (!BRL.equals(currency)) {
            throw new IllegalArgumentException("currency deve ser BRL");
        }
        if (cycle == BillingCycle.UNKNOWN) {
            throw new IllegalArgumentException("BillingCycle.UNKNOWN não pode ser enviado à API");
        }
        if (trialDays != null) {
            if (cycle == null) {
                throw new IllegalArgumentException("trialDays exige um cycle");
            }
            if (trialDays < 1 || trialDays > 90) {
                throw new IllegalArgumentException("trialDays deve ficar entre 1 e 90");
            }
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String externalId;
        private String name;
        private long price;
        private String description;
        private String imageUrl;
        private String fileUrl;
        private BillingCycle cycle;
        private Integer trialDays;

        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(long price) {
            this.price = price;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder fileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
            return this;
        }

        public Builder cycle(BillingCycle cycle) {
            this.cycle = cycle;
            return this;
        }

        public Builder trialDays(int trialDays) {
            this.trialDays = trialDays;
            return this;
        }

        public CreateProductRequest build() {
            return new CreateProductRequest(externalId, name, price, null, description,
                    imageUrl, fileUrl, cycle, trialDays);
        }
    }
}
