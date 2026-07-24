package com.example.temperature.controller;

import com.example.temperature.dto.query.TemperatureRecordQuery;
import com.example.temperature.dto.request.IngestTemperatureRecordsRequest;
import com.example.temperature.dto.response.IngestTemperatureRecordsResponse;
import com.example.temperature.dto.response.TemperatureRecordQueryResponse;
import com.example.temperature.service.TemperatureRecordService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping(path = "/api/v1/temperature-records", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class TemperatureRecordController {

    private final TemperatureRecordService service;

    public TemperatureRecordController(TemperatureRecordService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestTemperatureRecordsResponse> ingest(
            @Valid @RequestBody IngestTemperatureRecordsRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ingest(request));
    }

    @GetMapping
    public ResponseEntity<TemperatureRecordQueryResponse> query(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTimestamp,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTimestamp,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sortDirection
    ) {
        return ResponseEntity.ok(service.query(new TemperatureRecordQuery(
                startTimestamp,
                endTimestamp,
                limit,
                offset,
                sortDirection
        )));
    }
}
