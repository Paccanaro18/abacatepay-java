package io.github.paccanaro18.abacatepay.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.paccanaro18.abacatepay.checkouts.PaymentMethod;

public record PayerInformation(
        PaymentMethod method,
        @JsonProperty("PIX") PersonPayer pix,
        @JsonProperty("CARD") CardPayer card,
        @JsonProperty("BOLETO") PersonPayer boleto) {
}
