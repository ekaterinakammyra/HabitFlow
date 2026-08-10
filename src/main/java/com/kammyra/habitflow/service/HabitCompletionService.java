package com.kammyra.habitflow.service;

import com.kammyra.habitflow.dto.HabitCompletionResponse;
import com.kammyra.habitflow.dto.HabitStatisticsResponse;
import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.entity.HabitCompletion;
import com.kammyra.habitflow.exception.HabitNotFoundException;
import com.kammyra.habitflow.repository.HabitCompletionRepository;
import com.kammyra.habitflow.repository.HabitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HabitCompletionService {

    private final HabitCompletionRepository completionRepository;
    private final HabitRepository habitRepository;

    public HabitCompletionService(
            HabitCompletionRepository completionRepository,
            HabitRepository habitRepository
    ) {
        this.completionRepository = completionRepository;
        this.habitRepository = habitRepository;
    }

    public HabitCompletionResponse createCompletion(Long habitId) {

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        HabitCompletion completion = new HabitCompletion();

        completion.setHabit(habit);
        completion.setCompletedAt(LocalDateTime.now());

        HabitCompletion saved = completionRepository.save(completion);

        return new HabitCompletionResponse(
                saved.getId(),
                habit.getId(),
                saved.getCompletedAt()
        );
    }

    public List<HabitCompletionResponse> getCompletions(Long habitId) {

        if (!habitRepository.existsById(habitId)) {
            throw new HabitNotFoundException(habitId);
        }

        return completionRepository.findByHabitId(habitId)
                .stream()
                .map(completion -> new HabitCompletionResponse(
                        completion.getId(),
                        completion.getHabit().getId(),
                        completion.getCompletedAt()
                ))
                .toList();
    }

    public HabitStatisticsResponse getStatistics(Long habitId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        List<HabitCompletion> completions = completionRepository
                        .findByHabitIdOrderByCompletedAtAsc(habitId);

        long totalCompletions = completions.size();

        return new HabitStatisticsResponse(
                habit.getId(),
                habit.getName(),
                totalCompletions,
                0,
                0
        );
    }
}