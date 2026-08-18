package com.kammyra.habitflow.service;

import com.kammyra.habitflow.dto.HabitCompletionRequest;
import com.kammyra.habitflow.dto.HabitCompletionResponse;
import com.kammyra.habitflow.dto.HabitStatisticsResponse;
import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.entity.HabitCompletion;
import com.kammyra.habitflow.enums.Frequency;
import com.kammyra.habitflow.exception.FutureCompletionException;
import com.kammyra.habitflow.exception.HabitAlreadyCompletedException;
import com.kammyra.habitflow.exception.HabitNotFoundException;
import com.kammyra.habitflow.repository.HabitCompletionRepository;
import com.kammyra.habitflow.repository.HabitRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
public class HabitCompletionService {

    private final HabitCompletionRepository completionRepository;
    private final HabitRepository habitRepository;
    private final Clock clock;

    private LocalDate startOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public HabitCompletionService(
            HabitCompletionRepository completionRepository,
            HabitRepository habitRepository,
            Clock clock
    ) {
        this.completionRepository = completionRepository;
        this.habitRepository = habitRepository;
        this.clock = clock;
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
            completion.setCompletedAt(LocalDateTime.now(clock));
        }

        if (completion.getCompletedAt().isAfter(LocalDateTime.now(clock))) {
            throw new FutureCompletionException(completion.getCompletedAt());
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

        int currentStreak = calculateCurrentStreak(
                habit,
                completions
        );

        int bestStreak = calculateBestStreak(
                habit,
                completions
        );

        LocalDate lastCompletedDate = completions.stream()
                .map(HabitCompletion::getCompletionDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        return new HabitStatisticsResponse(
                habit.getId(),
                habit.getName(),
                totalCompletions,
                currentStreak,
                bestStreak,
                lastCompletedDate
        );
    }

    private int calculateCurrentStreak(
            Habit habit,
            List<HabitCompletion> completions
    ) {

        if (completions.isEmpty()) {
            return 0;
        }

        if (habit.getFrequency() == Frequency.DAILY) {
            return calculateDailyCurrentStreak(completions);
        }

        return calculateWeeklyCurrentStreak(completions);
    }

    private int calculateDailyCurrentStreak(
            List<HabitCompletion> completions
    ) {

        List<LocalDate> dates = completions.stream()
                .map(HabitCompletion::getCompletionDate)
                .distinct()
                .sorted()
                .toList();

        LocalDate today = LocalDate.now(clock);

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

    private int calculateWeeklyCurrentStreak(List<HabitCompletion> completions) {

        Set<LocalDate> completedWeeks = getCompletedWeeks(completions);

        if (completedWeeks.isEmpty()) {
            return 0;
        }

        List<LocalDate> weeks = completedWeeks.stream()
                .sorted()
                .toList();

        LocalDate lastWeek = weeks.get(weeks.size() - 1);

        int currentStreak = 1;

        for (int i = weeks.size() - 2; i >= 0; i--) {

            LocalDate previousWeek = weeks.get(i);

            if (previousWeek.plusWeeks(1).equals(lastWeek)) {
                currentStreak++;
                lastWeek = previousWeek;
            } else {
                break;
            }
        }

        return currentStreak;
    }

    private int calculateBestStreak(
            Habit habit,
            List<HabitCompletion> completions
    ) {

        if (completions.isEmpty()) {
            return 0;
        }

        if (habit.getFrequency() == Frequency.DAILY) {
            return calculateDailyBestStreak(completions);
        }

        return calculateWeeklyBestStreak(completions);
    }

    private int calculateDailyBestStreak(
            List<HabitCompletion> completions
    ) {

        List<LocalDate> dates = completions.stream()
                .map(HabitCompletion::getCompletionDate)
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

            bestStreak = Math.max(
                    bestStreak,
                    currentStreak
            );
        }

        return bestStreak;
    }

    private int calculateWeeklyBestStreak(List<HabitCompletion> completions) {

        Set<LocalDate> completedWeeks = getCompletedWeeks(completions);

        if (completedWeeks.isEmpty()) {
            return 0;
        }

        List<LocalDate> weeks = completedWeeks.stream()
                .sorted()
                .toList();

        int bestStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < weeks.size(); i++) {

            LocalDate previousWeek = weeks.get(i - 1);
            LocalDate currentWeek = weeks.get(i);

            if (previousWeek.plusWeeks(1).equals(currentWeek)) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }

            bestStreak = Math.max(bestStreak, currentStreak);
        }

        return bestStreak;
    }

    private Set<LocalDate> getCompletedWeeks(List<HabitCompletion> completions) {

        Set<LocalDate> weeks = new LinkedHashSet<>();

        for (HabitCompletion completion : completions) {
            LocalDate weekStart = startOfWeek(completion.getCompletionDate());
            weeks.add(weekStart);
        }

        return weeks;
    }

    public void completeHabit(Long habitId) {

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        LocalDate today = LocalDate.now(clock);

        boolean alreadyCompleted =
                completionRepository.existsByHabitIdAndCompletionDate(
                        habitId,
                        today
                );

        if (alreadyCompleted) {
            throw new IllegalStateException(
                    "Habit is already completed today"
            );
        }

        HabitCompletion completion = new HabitCompletion();

        completion.setHabit(habit);
        completion.setCompletionDate(today);

        completionRepository.save(completion);
    }
}