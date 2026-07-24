package com.example.temperature.dto.response;

public record ErrorDetailResponse(
        String field,
        String message
) {
}
