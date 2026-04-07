package com.solarlab.adboard.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class YandexDiskException extends RuntimeException {
    private final HttpStatusCode statusCode;
    private final String responseBody;

    public YandexDiskException(String message, HttpStatusCode statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}
