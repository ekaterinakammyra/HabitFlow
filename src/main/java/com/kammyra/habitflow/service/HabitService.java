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

    public Habit getHabitById(Long id) {
        return habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
    }

    public Habit updateHabit(Long id, Habit updatedHabit) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        habit.setName(updatedHabit.getName());
        habit.setDescription(updatedHabit.getDescription());
        habit.setFrequency(updatedHabit.getFrequency());

        return habitRepository.save(habit);
    }

    public void deleteHabit(Long id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        habitRepository.delete(habit);
    }
}
