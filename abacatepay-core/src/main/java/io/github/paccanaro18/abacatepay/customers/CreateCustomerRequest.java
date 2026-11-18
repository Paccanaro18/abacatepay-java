package io.github.paccanaro18.abacatepay.customers;

import java.util.Map;
import java.util.regex.Pattern;

public record CreateCustomerRequest(
        String email,
        String name,
        String cellphone,
        String taxId,
        String zipCode,
        Address address,
        Map<String, String> metadata) {

    private static final Pattern CEP = Pattern.compile("\\d{8}");

    public CreateCustomerRequest {
        if (blank(email)) {
            throw new IllegalArgumentException("O cliente precisa de um email");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("email inválido: " + email);
        }
        if (zipCode != null && !CEP.matcher(zipCode).matches()) {
            throw new IllegalArgumentException("zipCode deve ter 8 dígitos, sem hífen");
        }
        if (address != null) {
            validateAddress(address);
        }
        metadata = metadata == null ? null : Map.copyOf(metadata);
    }

    private static void validateAddress(Address address) {
        if (address.zipCode() == null || !CEP.matcher(address.zipCode()).matches()) {
            throw new IllegalArgumentException("address.zipCode deve ter 8 dígitos, sem hífen");
        }
        if (blank(address.street()) || blank(address.number()) || blank(address.neighborhood())
                || blank(address.city()) || blank(address.state())) {
            throw new IllegalArgumentException(
                    "O endereço precisa de street, number, neighborhood, city e state");
        }
        if (address.state().length() != 2) {
            throw new IllegalArgumentException("address.state deve ter 2 letras (UF)");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String email;
        private String name;
        private String cellphone;
        private String taxId;
        private String zipCode;
        private Address address;
        private Map<String, String> metadata;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder cellphone(String cellphone) {
            this.cellphone = cellphone;
            return this;
        }

        public Builder taxId(String taxId) {
            this.taxId = taxId;
            return this;
        }

        public Builder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public CreateCustomerRequest build() {
            return new CreateCustomerRequest(email, name, cellphone, taxId, zipCode, address, metadata);
        }
    }
}
