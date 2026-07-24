package com.example.temperature.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "temperature_records")
public class TemperatureRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "measured_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime measuredAt;

    @Column(name = "temperature", nullable = false, precision = 38, scale = 10)
    private BigDecimal temperature;

    protected TemperatureRecordEntity() {
    }

    public TemperatureRecordEntity(OffsetDateTime measuredAt, BigDecimal temperature) {
        this.measuredAt = measuredAt;
        this.temperature = temperature;
    }

    public Long getId() {
        return id;
    }

    public OffsetDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(OffsetDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }
}
