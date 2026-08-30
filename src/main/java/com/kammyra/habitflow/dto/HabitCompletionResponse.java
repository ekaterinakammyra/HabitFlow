package com.kammyra.habitflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HabitCompletionResponse {

    private Long id;
    private Long habitId;
    private LocalDate completionDate;
    private LocalDateTime completedAt;

    public HabitCompletionResponse(
            Long id,
            Long habitId,
            LocalDate completionDate,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.habitId = habitId;
        this.completionDate = completionDate;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getHabitId() {
        return habitId;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}