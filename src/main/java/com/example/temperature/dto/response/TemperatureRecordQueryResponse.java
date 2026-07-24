package com.example.temperature.dto.response;

import java.util.List;

public record TemperatureRecordQueryResponse(
        List<TemperatureRecordResponse> items,
        PageMetadataResponse page
) {
}
