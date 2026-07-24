package com.example.temperature.dto.response;

public record PageMetadataResponse(
        int limit,
        int offset,
        int count
) {
}
