package com.kammyra.habitflow.dto;

import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public class HabitCompletionRequest {

    @PastOrPresent(message = "Completion date cannot be in the future")
    private LocalDate completionDate;

    public HabitCompletionRequest() {
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }
}