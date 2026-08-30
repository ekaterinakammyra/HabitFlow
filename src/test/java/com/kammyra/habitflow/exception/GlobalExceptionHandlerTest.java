package com.kammyra.habitflow.exception;

import com.kammyra.habitflow.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GlobalExceptionHandlerTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(clock);

    @Test
    void shouldHandleHabitNotFound() {
        HabitNotFoundException exception =
                new HabitNotFoundException(1L);

        ResponseEntity<ErrorResponse> response =
                handler.handleHabitNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals(exception.getMessage(), response.getBody().getMessage());
    }

    @Test
    void shouldHandleHabitAlreadyCompleted() {
        HabitAlreadyCompletedException exception =
                new HabitAlreadyCompletedException(
                        1L,
                        LocalDate.of(2026, 8, 22)
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleHabitAlreadyCompleted(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals(exception.getMessage(), response.getBody().getMessage());
    }

    @Test
    void shouldHandleFutureCompletion() {
        LocalDate futureDate = LocalDate.of(2026, 8, 23);
        FutureCompletionException exception = new FutureCompletionException(futureDate);
        ResponseEntity<ErrorResponse> response = handler.handleFutureCompletion(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals(exception.getMessage(), response.getBody().getMessage());
    }
}