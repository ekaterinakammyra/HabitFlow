package com.kammyra.habitflow.dto;

import com.kammyra.habitflow.enums.Frequency;

import java.time.LocalDateTime;

public class HabitResponse {

    private Long id;
    private String name;
    private String description;
    private Frequency frequency;
    private LocalDateTime createdAt;

    public HabitResponse() {
    }

    public HabitResponse(
            Long id,
            String name,
            String description,
            Frequency frequency,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}