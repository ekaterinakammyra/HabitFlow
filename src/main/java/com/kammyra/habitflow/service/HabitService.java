package com.kammyra.habitflow.service;

import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.repository.HabitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HabitService {
    private final HabitRepository habitRepository;

    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public Habit createHabit(Habit habit) {
        habit.setCreatedAt(LocalDateTime.now());
        return habitRepository.save(habit);
    }

    public List<Habit> getAllHabits() {
        return habitRepository.findAll();
    }
}
