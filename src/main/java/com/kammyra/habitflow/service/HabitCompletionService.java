package com.kammyra.habitflow.service;

import com.kammyra.habitflow.dto.HabitCompletionRequest;
import com.kammyra.habitflow.dto.HabitCompletionResponse;
import com.kammyra.habitflow.dto.HabitStatisticsResponse;
import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.entity.HabitCompletion;
import com.kammyra.habitflow.exception.HabitAlreadyCompletedException;
import com.kammyra.habitflow.exception.HabitNotFoundException;
import com.kammyra.habitflow.repository.HabitCompletionRepository;
import com.kammyra.habitflow.repository.HabitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public HabitCompletionResponse createCompletion(
            Long habitId,
            HabitCompletionRequest request
    ) {

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        HabitCompletion completion = new HabitCompletion();

        completion.setHabit(habit);

        if (request != null && request.getCompletedAt() != null) {
            completion.setCompletedAt(request.getCompletedAt());
        } else {
            completion.setCompletedAt(LocalDateTime.now());
        }

        LocalDate completionDate =
                completion.getCompletedAt().toLocalDate();

        if (completionRepository.existsByHabitIdAndCompletionDate(
                habitId,
                completionDate
        )) {
            throw new HabitAlreadyCompletedException(
                    habitId,
                    completionDate
            );
        }

        completion.setCompletionDate(completionDate);

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

        int currentStreak = calculateCurrentStreak(completions);
        int bestStreak = calculateBestStreak(completions);

        return new HabitStatisticsResponse(
                habit.getId(),
                habit.getName(),
                totalCompletions,
                currentStreak,
                bestStreak
        );
    }

    private int calculateCurrentStreak(List<HabitCompletion> completions) {

        if (completions.isEmpty()) {
            return 0;
        }

        List<LocalDate> dates = completions.stream()
                .map(completion -> completion.getCompletedAt().toLocalDate())
                .distinct()
                .toList();

        LocalDate today = LocalDate.now();

        int streak = 0;
        LocalDate expectedDate = today;

        for (int i = dates.size() - 1; i >= 0; i--) {

            LocalDate date = dates.get(i);

            if (date.equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else if (date.isBefore(expectedDate)) {
                break;
            }
        }

        return streak;
    }

    private int calculateBestStreak(List<HabitCompletion> completions) {

        if (completions.isEmpty()) {
            return 0;
        }

        List<LocalDate> dates = completions.stream()
                .map(completion -> completion.getCompletedAt().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        int currentStreak = 1;
        int bestStreak = 1;

        for (int i = 1; i < dates.size(); i++) {

            LocalDate previousDate = dates.get(i - 1);
            LocalDate currentDate = dates.get(i);

            if (currentDate.equals(previousDate.plusDays(1))) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }

            bestStreak = Math.max(bestStreak, currentStreak);
        }

        return bestStreak;
    }
}