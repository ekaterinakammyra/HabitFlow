package com.kammyra.habitflow.dto;

import java.time.LocalDateTime;

public class HabitCompletionResponse {

    private Long id;
    private Long habitId;
    private LocalDateTime completedAt;

    public HabitCompletionResponse(
            Long id,
            Long habitId,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.habitId = habitId;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getHabitId() {
        return habitId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

}