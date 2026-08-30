package com.kammyra.habitflow.exception;

import java.time.LocalDate;

public class HabitAlreadyCompletedException extends IllegalStateException {

    public HabitAlreadyCompletedException(
            Long habitId,
            LocalDate date
    ) {
        super("Habit with id " + habitId + " is already completed for " + date);
    }
}