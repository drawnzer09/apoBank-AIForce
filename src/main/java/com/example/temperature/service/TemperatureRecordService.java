package com.example.temperature.service;

import com.example.temperature.dto.query.TemperatureRecordQuery;
import com.example.temperature.dto.request.IngestTemperatureRecordsRequest;
import com.example.temperature.dto.response.ErrorDetailResponse;
import com.example.temperature.dto.response.IngestTemperatureRecordsResponse;
import com.example.temperature.dto.response.PageMetadataResponse;
import com.example.temperature.dto.response.TemperatureRecordQueryResponse;
import com.example.temperature.dto.response.TemperatureRecordResponse;
import com.example.temperature.entity.TemperatureRecordEntity;
import com.example.temperature.exception.InvalidQueryParameterException;
import com.example.temperature.exception.StorageUnavailableException;
import com.example.temperature.mapper.TemperatureRecordMapper;
import com.example.temperature.repository.TemperatureRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TemperatureRecordService {

    private static final Logger log = LoggerFactory.getLogger(TemperatureRecordService.class);
    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;
    private static final int DEFAULT_OFFSET = 0;
    private static final String DEFAULT_SORT_DIRECTION = "asc";

    private final TemperatureRecordRepository repository;
    private final TemperatureRecordMapper mapper;

    public TemperatureRecordService(TemperatureRecordRepository repository, TemperatureRecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public IngestTemperatureRecordsResponse ingest(IngestTemperatureRecordsRequest request) {
        log.info("Ingesting {} temperature records", request.records().size());

        try {
            List<TemperatureRecordEntity> savedEntities = repository.saveAll(mapper.toEntities(request.records()));
            repository.flush();
            List<TemperatureRecordResponse> records = mapper.toResponses(savedEntities);
            return new IngestTemperatureRecordsResponse(records.size(), records);
        } catch (DataAccessException ex) {
            log.error("Failed to persist temperature records", ex);
            throw new StorageUnavailableException("Temperature records could not be persisted", ex);
        }
    }

    @Transactional(readOnly = true)
    public TemperatureRecordQueryResponse query(TemperatureRecordQuery query) {
        NormalizedQuery normalized = normalizeAndValidate(query);

        log.info(
                "Querying temperature records from {} to {} with limit={}, offset={}, sortDirection={}",
                normalized.startTimestamp(),
                normalized.endTimestamp(),
                normalized.limit(),
                normalized.offset(),
                normalized.sortDirection()
        );

        try {
            List<TemperatureRecordEntity> entities = "desc".equals(normalized.sortDirection())
                    ? repository.findByMeasuredAtRangeOrderByMeasuredAtDesc(
                            normalized.startTimestamp(),
                            normalized.endTimestamp(),
                            normalized.limit(),
                            normalized.offset()
                    )
                    : repository.findByMeasuredAtRangeOrderByMeasuredAtAsc(
                            normalized.startTimestamp(),
                            normalized.endTimestamp(),
                            normalized.limit(),
                            normalized.offset()
                    );

            List<TemperatureRecordResponse> items = mapper.toResponses(entities);
            return new TemperatureRecordQueryResponse(
                    items,
                    new PageMetadataResponse(normalized.limit(), normalized.offset(), items.size())
            );
        } catch (DataAccessException ex) {
            log.error("Failed to query temperature records", ex);
            throw new StorageUnavailableException("Temperature records could not be queried", ex);
        }
    }

    private NormalizedQuery normalizeAndValidate(TemperatureRecordQuery query) {
        List<ErrorDetailResponse> details = new ArrayList<>();

        if (query.startTimestamp() == null) {
            details.add(new ErrorDetailResponse("startTimestamp", "startTimestamp is required"));
        }
        if (query.endTimestamp() == null) {
            details.add(new ErrorDetailResponse("endTimestamp", "endTimestamp is required"));
        }
        if (query.startTimestamp() != null
                && query.endTimestamp() != null
                && query.endTimestamp().isBefore(query.startTimestamp())) {
            details.add(new ErrorDetailResponse(
                    "endTimestamp",
                    "endTimestamp must be greater than or equal to startTimestamp"
            ));
        }

        int limit = query.limit() == null ? DEFAULT_LIMIT : query.limit();
        if (limit < 1 || limit > MAX_LIMIT) {
            details.add(new ErrorDetailResponse("limit", "limit must be between 1 and 1000"));
        }

        int offset = query.offset() == null ? DEFAULT_OFFSET : query.offset();
        if (offset < 0) {
            details.add(new ErrorDetailResponse("offset", "offset must be greater than or equal to 0"));
        }

        String sortDirection = query.sortDirection() == null || query.sortDirection().isBlank()
                ? DEFAULT_SORT_DIRECTION
                : query.sortDirection().trim().toLowerCase();

        if (!"asc".equals(sortDirection) && !"desc".equals(sortDirection)) {
            details.add(new ErrorDetailResponse("sortDirection", "sortDirection must be either asc or desc"));
        }

        if (!details.isEmpty()) {
            throw new InvalidQueryParameterException("Invalid query parameters", details);
        }

        return new NormalizedQuery(query.startTimestamp(), query.endTimestamp(), limit, offset, sortDirection);
    }

    private record NormalizedQuery(
            OffsetDateTime startTimestamp,
            OffsetDateTime endTimestamp,
            int limit,
            int offset,
            String sortDirection
    ) {
    }
}
