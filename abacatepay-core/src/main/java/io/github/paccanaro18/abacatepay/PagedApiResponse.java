package io.github.paccanaro18.abacatepay;

import java.util.List;

public record PagedApiResponse<T>(boolean success, List<T> data, String error, Pagination pagination) {

    public record Pagination(boolean hasMore, String next, String before) {
    }
}
