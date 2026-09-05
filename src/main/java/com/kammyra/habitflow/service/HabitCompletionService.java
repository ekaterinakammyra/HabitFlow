package com.kammyra.habitflow.service;

import com.kammyra.habitflow.dto.HabitCompletionRequest;
import com.kammyra.habitflow.dto.HabitCompletionResponse;
import com.kammyra.habitflow.dto.HabitStatisticsResponse;
import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.entity.HabitCompletion;
import com.kammyra.habitflow.enums.Frequency;
import com.kammyra.habitflow.exception.CompletionBeforeHabitCreationException;
import com.kammyra.habitflow.exception.FutureCompletionException;
import com.kammyra.habitflow.exception.HabitAlreadyCompletedException;
import com.kammyra.habitflow.exception.HabitNotFoundException;
import com.kammyra.habitflow.repository.HabitCompletionRepository;
import com.kammyra.habitflow.repository.HabitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class HabitCompletionService {

    private final HabitCompletionRepository completionRepository;
    private final HabitRepository habitRepository;
    private final Clock clock;

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

        LocalDate today = LocalDate.now(clock);
        LocalDate completionDate = today;

        if (request != null && request.getCompletionDate() != null) {
            completionDate = request.getCompletionDate();
        }

        if (completionDate.isAfter(today)) {
            throw new FutureCompletionException(completionDate);
        }

        validateCompletionDate(
                habit,
                completionDate
        );

        validateCompletion(
                habit,
                habitId,
                completionDate
        );

        HabitCompletion completion = new HabitCompletion();

        completion.setHabit(habit);
        completion.setCompletionDate(completionDate);
        completion.setCompletedAt(LocalDateTime.now(clock));

        HabitCompletion saved = completionRepository.save(completion);
        return toResponse(saved);
    }

    private void validateCompletion(
            Habit habit,
            Long habitId,
            LocalDate completionDate
    ) {

        if (habit.getFrequency() == Frequency.DAILY) {
            validateDailyCompletion(
                    habitId,
                    completionDate
            );
            return;
        }

        validateWeeklyCompletion(
                habitId,
                completionDate
        );
    }

    private void validateDailyCompletion(
            Long habitId,
            LocalDate completionDate
    ) {

        if (completionRepository.existsByHabitIdAndCompletionDate(
                habitId,
                completionDate
        )) {
            throw new HabitAlreadyCompletedException(
                    habitId,
                    completionDate
            );
        }
    }

    private void validateWeeklyCompletion(
            Long habitId,
            LocalDate completionDate
    ) {

        LocalDate weekStart = completionDate
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate weekEnd = weekStart.plusDays(6);

        if (completionRepository.existsByHabitIdAndCompletionDateBetween(
                habitId,
                weekStart,
                weekEnd
        )) {
            throw new HabitAlreadyCompletedException(
                    habitId,
                    completionDate
            );
        }
    }

    @Transactional(readOnly = true)
    public List<HabitCompletionResponse> getCompletions(Long habitId) {

        if (!habitRepository.existsById(habitId)) {
            throw new HabitNotFoundException(habitId);
        }
        return completionRepository.findByHabitIdWithHabit(habitId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitStatisticsResponse getStatistics(Long habitId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        List<HabitCompletion> completions = completionRepository
                .findByHabitIdOrderByCompletionDateAsc(habitId);

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

    private HabitCompletionResponse toResponse(HabitCompletion completion) {
        return new HabitCompletionResponse(
                completion.getId(),
                completion.getHabit().getId(),
                completion.getCompletionDate(),
                completion.getCompletedAt()
        );
    }

    private int calculateCurrentStreak(Habit habit, List<HabitCompletion> completions) {
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

        if (dates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate latestDate = dates.get(dates.size() - 1);

        if (!latestDate.isEqual(today) && !latestDate.isEqual(today.minusDays(1))) {
            return 0;
        }

        int streak = 1;
        LocalDate expectedPrevious = latestDate.minusDays(1);

        for (int i = dates.size() - 2; i >= 0; i--) {
            LocalDate currentDate = dates.get(i);
            if (currentDate.isEqual(expectedPrevious)) {
                streak++;
                expectedPrevious = expectedPrevious.minusDays(1);
            } else {
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

        LocalDate today = LocalDate.now(clock);
        LocalDate currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastWeekStart = currentWeekStart.minusWeeks(1);

        LocalDate latestWeek = weeks.get(weeks.size() - 1);

        if (!latestWeek.isEqual(currentWeekStart) && !latestWeek.isEqual(lastWeekStart)) {
            return 0;
        }

        int streak = 1;
        LocalDate expectedPreviousWeek = latestWeek.minusWeeks(1);
        for (int i = weeks.size() - 2; i >= 0; i--) {
            LocalDate currentWeek = weeks.get(i);
            if (currentWeek.isEqual(expectedPreviousWeek)) {
                streak++;
                expectedPreviousWeek = expectedPreviousWeek.minusWeeks(1);
            } else {
                break;
            }
        }

        return streak;
    }

    private int calculateBestStreak(Habit habit, List<HabitCompletion> completions) {
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

        if (dates.isEmpty()) {
            return 0;
        }

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

            if (currentWeek.equals(previousWeek.plusWeeks(1))) {
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
            LocalDate weekStart = completion.getCompletionDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            weeks.add(weekStart);
        }

        return weeks;
    }

    public void completeHabit(Long habitId) {
        createCompletion(habitId, null);
    }

    private void validateCompletionDate(
            Habit habit,
            LocalDate completionDate
    ) {

        LocalDate habitCreationDate =
                habit.getCreatedAt().toLocalDate();

        if (completionDate.isBefore(habitCreationDate)) {
            throw new CompletionBeforeHabitCreationException(
                    completionDate,
                    habitCreationDate
            );
        }
    }
}