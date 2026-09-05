package com.kammyra.habitflow.exception;

import java.time.LocalDate;

public class CompletionBeforeHabitCreationException extends RuntimeException {

    private final LocalDate completionDate;

    private final LocalDate habitCreationDate;

    public CompletionBeforeHabitCreationException(
            LocalDate completionDate,
            LocalDate habitCreationDate
    ) {
        super(
                "Completion date " + completionDate
                        + " cannot be before habit creation date "
                        + habitCreationDate
        );

        this.completionDate = completionDate;
        this.habitCreationDate = habitCreationDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public LocalDate getHabitCreationDate() {
        return habitCreationDate;
    }
}