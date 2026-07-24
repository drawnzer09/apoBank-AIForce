package com.example.temperature.exception;

import com.example.temperature.dto.response.ErrorDetailResponse;

import java.util.List;

public class InvalidQueryParameterException extends RuntimeException {

    private final List<ErrorDetailResponse> details;

    public InvalidQueryParameterException(String message, List<ErrorDetailResponse> details) {
        super(message);
        this.details = List.copyOf(details);
    }

    public List<ErrorDetailResponse> getDetails() {
        return details;
    }
}
