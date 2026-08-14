package com.kammyra.habitflow.exception;

import java.time.LocalDateTime;

public class FutureCompletionException extends RuntimeException {

    public FutureCompletionException(LocalDateTime completedAt) {
        super("Completion date cannot be in the future: " + completedAt);
    }
}