package com.example.temperature.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Error error
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(
            String code,
            String message,
            List<ErrorDetailResponse> details
    ) {
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new Error(code, message, null));
    }

    public static ErrorResponse of(String code, String message, List<ErrorDetailResponse> details) {
        return new ErrorResponse(new Error(code, message, details == null || details.isEmpty() ? null : details));
    }
}
