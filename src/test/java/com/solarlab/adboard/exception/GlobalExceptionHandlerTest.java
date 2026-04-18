package com.solarlab.adboard.exception;

import com.solarlab.adboard.dto.response.ExceptionResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleMainScenarios() throws Exception {
        assertEquals(502, handler.handleYandexDiskError(
                new YandexDiskException("fail", HttpStatusCode.valueOf(502), "body")
        ).getStatusCode().value());
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleNotFound(new EntityNotFoundException("missing")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleIllegalArgument(new IllegalArgumentException("bad")).getStatusCode());

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(),
                "target");
        bindingResult.addError(new FieldError("target", "email", "must be valid"));
        Method method = SampleMethods.class.getDeclaredMethod("sample", String.class);
        MethodArgumentNotValidException invalidException = new MethodArgumentNotValidException(
                new MethodParameter(method, 0), bindingResult
        );
        ResponseEntity<ExceptionResponse> invalidResponse = handler.handleValidation(invalidException);
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode());
        assertEquals("Field 'email': must be valid", invalidResponse.getBody().message());

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("invalid");
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleConstraintViolation(
                new ConstraintViolationException(Set.of(violation))
        ).getStatusCode());

        MethodArgumentTypeMismatchException mismatchException = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", new MethodParameter(method, 0), null
        );
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleArgumentTypeMismatch(mismatchException).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleMissingRequestPart(new MissingServletRequestPartException("file"))
                        .getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleMultipartError(new MultipartException("bad")).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                handler.handleAccessDenied(new AccessDeniedException("denied")).getStatusCode());
    }

    @Test
    void shouldHandleInfrastructureAndHttpErrors() {
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                handler.handleServiceUnavailable(new ResourceAccessException("down")).getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                handler.handleServiceUnavailable(new DataAccessResourceFailureException("db"))
                        .getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                handler.handleServiceUnavailable(new CannotCreateTransactionException("tx"))
                        .getStatusCode());

        HttpClientErrorException clientException = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                null,
                new byte[0],
                StandardCharsets.UTF_8
        );
        HttpServerErrorException serverException = HttpServerErrorException.create(
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                null,
                new byte[0],
                StandardCharsets.UTF_8
        );
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleHttpClientError(clientException)
                .getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY, handler.handleHttpServerError(serverException)
                .getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                handler.handleAll(new RuntimeException("boom")).getStatusCode());
    }

    static class SampleMethods {
        @SuppressWarnings("unused")
        void sample(String value) {
        }
    }
}
