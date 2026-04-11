package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.response.ExceptionResponse;
import com.solarlab.adboard.exception.YandexDiskException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

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

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .message(message)
                        .status(HttpStatus.BAD_REQUEST.value())
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
