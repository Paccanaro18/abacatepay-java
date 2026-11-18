package io.github.paccanaro18.abacatepay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public final class AbacatePayJson {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    private AbacatePayJson() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
