package com.example.temperature.repository;

import com.example.temperature.entity.TemperatureRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface TemperatureRecordRepository extends JpaRepository<TemperatureRecordEntity, Long> {

    @Query(
            value = """
                    SELECT id, measured_at, temperature
                    FROM temperature_records
                    WHERE measured_at BETWEEN :startTimestamp AND :endTimestamp
                    ORDER BY measured_at ASC
                    LIMIT :limit OFFSET :offset
                    """,
            nativeQuery = true
    )
    List<TemperatureRecordEntity> findByMeasuredAtRangeOrderByMeasuredAtAsc(
            @Param("startTimestamp") OffsetDateTime startTimestamp,
            @Param("endTimestamp") OffsetDateTime endTimestamp,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(
            value = """
                    SELECT id, measured_at, temperature
                    FROM temperature_records
                    WHERE measured_at BETWEEN :startTimestamp AND :endTimestamp
                    ORDER BY measured_at DESC
                    LIMIT :limit OFFSET :offset
                    """,
            nativeQuery = true
    )
    List<TemperatureRecordEntity> findByMeasuredAtRangeOrderByMeasuredAtDesc(
            @Param("startTimestamp") OffsetDateTime startTimestamp,
            @Param("endTimestamp") OffsetDateTime endTimestamp,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
