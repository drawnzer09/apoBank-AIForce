package com.example.temperature.mapper;

import com.example.temperature.dto.request.TemperatureRecordRequest;
import com.example.temperature.dto.response.TemperatureRecordResponse;
import com.example.temperature.entity.TemperatureRecordEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemperatureRecordMapper {

    public TemperatureRecordEntity toEntity(TemperatureRecordRequest request) {
        return new TemperatureRecordEntity(request.timestamp(), request.temperature());
    }

    public List<TemperatureRecordEntity> toEntities(List<TemperatureRecordRequest> requests) {
        return requests.stream()
                .map(this::toEntity)
                .toList();
    }

    public TemperatureRecordResponse toResponse(TemperatureRecordEntity entity) {
        return new TemperatureRecordResponse(entity.getMeasuredAt(), entity.getTemperature());
    }

    public List<TemperatureRecordResponse> toResponses(List<TemperatureRecordEntity> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
