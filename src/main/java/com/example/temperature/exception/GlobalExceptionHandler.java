package com.example.temperature.exception;

import com.example.temperature.dto.response.ErrorDetailResponse;
import com.example.temperature.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<ErrorDetailResponse> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorDetailResponse(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        boolean payloadTooLarge = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .anyMatch(this::isRecordsMaxSizeViolation);

        if (payloadTooLarge) {
            log.warn("Payload too large: {}", details);
            return build(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "PAYLOAD_TOO_LARGE",
                    "Ingest request contains more than 1,000 records",
                    details
            );
        }

        log.warn("Request validation failed: {}", details);
        return build(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR",
                "Request validation failed",
                details
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        UnrecognizedPropertyException unrecognizedPropertyException = findCause(
                ex,
                UnrecognizedPropertyException.class
        );

        if (unrecognizedPropertyException != null) {
            String fieldName = unrecognizedPropertyException.getPropertyName();
            List<ErrorDetailResponse> details = List.of(
                    new ErrorDetailResponse(fieldName, "Unknown field is not allowed")
            );

            log.warn("Unknown JSON field rejected: {}", fieldName);
            return build(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR",
                    "Request validation failed",
                    details
            );
        }

        log.warn("Malformed or unreadable request body", ex);
        return build(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Malformed or invalid JSON request body",
                List.of()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<ErrorDetailResponse> details = List.of(
                new ErrorDetailResponse(ex.getParameterName(), ex.getParameterName() + " is required")
        );

        log.warn("Missing request parameter: {}", ex.getParameterName());
        return build(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Missing required query parameter",
                details
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.warn("Unsupported media type: {}", ex.getContentType());
        return build(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE",
                "Unsupported media type",
                List.of()
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.warn("Method not allowed: {}", ex.getMethod());
        return build(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "HTTP method is not allowed for this endpoint",
                List.of()
        );
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.warn("API path not found: {}", ex.getRequestURL());
        return build(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                "API path does not exist",
                List.of()
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getResourcePath());
        return build(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                "API path does not exist",
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String field = ex.getName();
        List<ErrorDetailResponse> details = List.of(
                new ErrorDetailResponse(field, field + " has an invalid value")
        );

        log.warn("Query parameter type mismatch: {}", field);
        return build(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Invalid query parameter",
                details
        );
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<Object> handleConversionFailed(ConversionFailedException ex) {
        log.warn("Request parameter conversion failed", ex);
        return build(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Invalid query parameter",
                List.of()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorDetailResponse> details = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorDetailResponse(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        log.warn("Constraint violation: {}", details);
        return build(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Invalid request parameters",
                details
        );
    }

    @ExceptionHandler(InvalidQueryParameterException.class)
    public ResponseEntity<Object> handleInvalidQueryParameter(InvalidQueryParameterException ex) {
        log.warn("Invalid query parameters: {}", ex.getDetails());
        return build(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                ex.getMessage(),
                ex.getDetails()
        );
    }

    @ExceptionHandler(StorageUnavailableException.class)
    public ResponseEntity<Object> handleStorageUnavailable(StorageUnavailableException ex) {
        log.error("Storage unavailable", ex);
        return build(
                HttpStatus.SERVICE_UNAVAILABLE,
                "SERVICE_UNAVAILABLE",
                "Storage is temporarily unavailable",
                List.of()
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccess(DataAccessException ex) {
        log.error("Database access failure", ex);
        return build(
                HttpStatus.SERVICE_UNAVAILABLE,
                "SERVICE_UNAVAILABLE",
                "Storage is temporarily unavailable",
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        log.error("Unexpected server error", ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Unexpected server error",
                List.of()
        );
    }

    private boolean isRecordsMaxSizeViolation(FieldError fieldError) {
        Object rejectedValue = fieldError.getRejectedValue();
        if (!"records".equals(fieldError.getField()) || !(rejectedValue instanceof Collection<?> collection)) {
            return false;
        }

        String[] codes = fieldError.getCodes();
        boolean sizeViolation = false;
        if (codes != null) {
            for (String code : codes) {
                if (code != null && code.contains("Size")) {
                    sizeViolation = true;
                    break;
                }
            }
        }

        return sizeViolation && collection.size() > 1000;
    }

    private ResponseEntity<Object> build(
            HttpStatus status,
            String code,
            String message,
            List<ErrorDetailResponse> details
    ) {
        ErrorResponse response = ErrorResponse.of(code, message, details);
        return ResponseEntity.status(status).body(response);
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> causeType) {
        List<Throwable> visited = new ArrayList<>();
        Throwable current = throwable;

        while (current != null && !visited.contains(current)) {
            if (causeType.isInstance(current)) {
                return causeType.cast(current);
            }

            visited.add(current);
            current = current.getCause();
        }

        return null;
    }
}
