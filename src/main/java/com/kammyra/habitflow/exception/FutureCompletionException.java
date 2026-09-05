package com.kammyra.habitflow.exception;

import java.time.LocalDate;

public class FutureCompletionException extends RuntimeException {
    public FutureCompletionException(LocalDate completionDate) {
        super("Completion date cannot be in the future: " + completionDate);
    }
}