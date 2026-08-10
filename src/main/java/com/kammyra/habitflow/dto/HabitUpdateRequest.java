package com.kammyra.habitflow.dto;

import com.kammyra.habitflow.enums.Frequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HabitUpdateRequest {

    @NotBlank(message = "Name must not be blank")
    private String name;

    private String description;

    @NotNull(message = "Frequency must not be null")
    private Frequency frequency;

    public HabitUpdateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }
}