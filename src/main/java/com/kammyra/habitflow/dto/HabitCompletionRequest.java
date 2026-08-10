package com.kammyra.habitflow.dto;

import java.time.LocalDateTime;

public class HabitCompletionRequest {

    private LocalDateTime completedAt;

    public HabitCompletionRequest() {
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}