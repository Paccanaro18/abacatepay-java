package io.github.paccanaro18.abacatepay.subscriptions;

/** Multa única após o vencimento: centésimos de percentual (PERCENTAGE) ou centavos (FIXED). */
public record BoletoFine(int value, FineType type) {
}
