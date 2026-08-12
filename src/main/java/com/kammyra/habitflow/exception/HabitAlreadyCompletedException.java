package com.kammyra.habitflow.exception;

public class HabitAlreadyCompletedException extends RuntimeException {

    public HabitAlreadyCompletedException(Long habitId, java.time.LocalDate date) {
        super("Habit with id " + habitId + " is already completed for " + date);
    }
}