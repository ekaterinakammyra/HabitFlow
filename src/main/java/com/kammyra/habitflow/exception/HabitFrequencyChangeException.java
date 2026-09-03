package com.kammyra.habitflow.exception;

public class HabitFrequencyChangeException extends RuntimeException {

    public HabitFrequencyChangeException() {
        super("Cannot change habit frequency after it has been completed");
    }
}