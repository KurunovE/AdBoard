package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.response.ExceptionResponse;
import com.solarlab.adboard.exception.YandexDiskException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(YandexDiskException.class)
    public ResponseEntity<ExceptionResponse> handleYandexDiskError(YandexDiskException ex) {
        log.error("Yandex Disk Error: {} - {}", ex.getStatusCode(), ex.getResponseBody());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ExceptionResponse.builder()
                        .message("Yandex Disk operation failed: " + ex.getMessage())
                        .status(ex.getStatusCode().value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponse.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(this::buildFieldErrorMessage)
                .orElse("Validation failed");

        log.warn("Request body validation failed: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .message(message)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Validation failed");

        log.warn("Constraint violation: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .message(message)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        String message = "Parameter '" + ex.getName() + "' has invalid value";

        log.warn("Argument type mismatch for parameter={} value={}", ex.getName(), ex.getValue());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .message(message)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {
        ex.getMostSpecificCause();
        String message = ex.getMostSpecificCause().getMessage();

        log.warn("Data integrity violation: {}", message);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ExceptionResponse.builder()
                        .message(message)
                        .status(HttpStatus.CONFLICT.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ExceptionResponse> handleMissingRequestPart(
            MissingServletRequestPartException ex
    ) {
        log.warn("Missing request part: {}", ex.getRequestPartName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .message("Missing request part: " + ex.getRequestPartName())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ExceptionResponse> handleMultipartError(MultipartException ex) {
        log.warn("Invalid multipart request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .message("Invalid multipart request")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ExceptionResponse.builder()
                        .message("Access denied")
                        .status(HttpStatus.FORBIDDEN.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler({
            ResourceAccessException.class,
            DataAccessResourceFailureException.class,
            CannotCreateTransactionException.class
    })
    public ResponseEntity<ExceptionResponse> handleServiceUnavailable(Exception ex) {
        log.error("Service unavailable", ex);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ExceptionResponse.builder()
                        .message("Dependent service is temporarily unavailable")
                        .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ExceptionResponse> handleHttpClientError(HttpClientErrorException ex) {
        log.error("HTTP Client Error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ExceptionResponse.builder()
                        .message("Client error from external service: " + ex.getStatusText())
                        .status(ex.getStatusCode().value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ExceptionResponse> handleHttpServerError(HttpServerErrorException ex) {
        log.error("HTTP Server Error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ExceptionResponse.builder()
                        .message("Server error from external service")
                        .status(ex.getStatusCode().value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleAll(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse.builder()
                        .message("An unexpected error occurred. Please contact support.")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    private String buildFieldErrorMessage(FieldError fieldError) {
        return "Field '" + fieldError.getField() + "': " + fieldError.getDefaultMessage();
    }
}
