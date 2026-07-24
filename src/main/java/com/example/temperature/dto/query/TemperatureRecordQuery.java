package com.example.temperature.dto.query;

import java.time.OffsetDateTime;

public record TemperatureRecordQuery(
        OffsetDateTime startTimestamp,
        OffsetDateTime endTimestamp,
        Integer limit,
        Integer offset,
        String sortDirection
) {
}
