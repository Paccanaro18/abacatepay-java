package io.github.paccanaro18.abacatepay;

public record ApiResponse<T>(boolean success, T data, String error) {
}
