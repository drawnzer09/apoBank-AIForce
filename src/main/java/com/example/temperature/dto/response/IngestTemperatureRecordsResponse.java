package com.example.temperature.dto.response;

import java.util.List;

public record IngestTemperatureRecordsResponse(
        int acceptedCount,
        List<TemperatureRecordResponse> records
) {
}
