package com.kammyra.habitflow.entity;

import com.kammyra.habitflow.enums.Frequency;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    private LocalDateTime createdAt;

    public Habit() {}
    public Habit(String name, String description, Frequency frequency, LocalDateTime createdAt) {
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
