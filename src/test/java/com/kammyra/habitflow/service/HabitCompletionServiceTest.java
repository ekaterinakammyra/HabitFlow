package com.kammyra.habitflow.service;

import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.entity.HabitCompletion;
import com.kammyra.habitflow.enums.Frequency;
import com.kammyra.habitflow.repository.HabitCompletionRepository;
import com.kammyra.habitflow.repository.HabitRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HabitCompletionServiceTest {

    private final HabitCompletionRepository completionRepository =
            mock(HabitCompletionRepository.class);

    private final HabitRepository habitRepository =
            mock(HabitRepository.class);

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-08-14T10:00:00Z"),
            ZoneId.of("UTC")
    );

    private final HabitCompletionService service =
            new HabitCompletionService(
                    completionRepository,
                    habitRepository,
                    clock
            );

    @Test
    void shouldReturnZeroStreakWhenThereAreNoCompletions() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L))
                .thenReturn(java.util.Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletedAtAsc(1L))
                .thenReturn(List.of());

        var result = service.getStatistics(1L);

        assertEquals(0, result.getCurrentStreak());
        assertEquals(0, result.getBestStreak());
        assertEquals(0, result.getTotalCompletions());
    }

    private HabitCompletion completion(LocalDate date) {

        HabitCompletion completion = new HabitCompletion();

        completion.setCompletionDate(date);

        return completion;
    }

    @Test
    void shouldReturnCurrentStreakOneWhenCompletedToday() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L))
                .thenReturn(java.util.Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletedAtAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 8, 14))
                ));

        var result = service.getStatistics(1L);

        assertEquals(1, result.getCurrentStreak());
        assertEquals(1, result.getBestStreak());
        assertEquals(1, result.getTotalCompletions());
    }

    @Test
    void shouldReturnCurrentStreakTwoForTwoConsecutiveDays() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L))
                .thenReturn(java.util.Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletedAtAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 8, 13)),
                        completion(LocalDate.of(2026, 8, 14))
                ));

        var result = service.getStatistics(1L);

        assertEquals(2, result.getCurrentStreak());
        assertEquals(2, result.getBestStreak());
    }

    @Test
    void shouldReturnCurrentStreakThreeForThreeConsecutiveDays() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L))
                .thenReturn(java.util.Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletedAtAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 8, 12)),
                        completion(LocalDate.of(2026, 8, 13)),
                        completion(LocalDate.of(2026, 8, 14))
                ));

        var result = service.getStatistics(1L);

        assertEquals(3, result.getCurrentStreak());
        assertEquals(3, result.getBestStreak());
    }

    @Test
    void shouldResetCurrentStreakAfterGap() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L))
                .thenReturn(java.util.Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletedAtAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 8, 10)),
                        completion(LocalDate.of(2026, 8, 11)),
                        completion(LocalDate.of(2026, 8, 13)),
                        completion(LocalDate.of(2026, 8, 14))
                ));

        var result = service.getStatistics(1L);

        assertEquals(2, result.getCurrentStreak());
        assertEquals(2, result.getBestStreak());
    }

    @Test
    void shouldKeepBestStreakAfterCurrentStreakIsBroken() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L))
                .thenReturn(java.util.Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletedAtAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 8, 10)),
                        completion(LocalDate.of(2026, 8, 11)),
                        completion(LocalDate.of(2026, 8, 12)),
                        completion(LocalDate.of(2026, 8, 14))
                ));

        var result = service.getStatistics(1L);

        assertEquals(1, result.getCurrentStreak());
        assertEquals(3, result.getBestStreak());
    }

    @Test
    void shouldReturnCurrentWeeklyStreakThreeForThreeConsecutiveWeeks() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Тренировка");
        habit.setFrequency(Frequency.WEEKLY);

        when(habitRepository.findById(1L))
                .thenReturn(java.util.Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletedAtAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 7, 27)),
                        completion(LocalDate.of(2026, 8, 3)),
                        completion(LocalDate.of(2026, 8, 10))
                ));

        var result = service.getStatistics(1L);

        assertEquals(3, result.getCurrentStreak());
        assertEquals(3, result.getBestStreak());
        assertEquals(3, result.getTotalCompletions());
    }

    @Test
    void shouldResetWeeklyCurrentStreakAfterGap() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Тренировка");
        habit.setFrequency(Frequency.WEEKLY);

        when(habitRepository.findById(1L))
                .thenReturn(java.util.Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletedAtAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 7, 27)),
                        completion(LocalDate.of(2026, 8, 3)),
                        completion(LocalDate.of(2026, 8, 17)),
                        completion(LocalDate.of(2026, 8, 24))
                ));

        var result = service.getStatistics(1L);

        assertEquals(2, result.getCurrentStreak());
        assertEquals(2, result.getBestStreak());
        assertEquals(4, result.getTotalCompletions());
    }

    @Test
    void shouldCompleteHabitSuccessfully() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L))
                .thenReturn(java.util.Optional.of(habit));

        when(completionRepository.existsByHabitIdAndCompletionDate(
                1L,
                LocalDate.of(2026, 8, 14)
        )).thenReturn(false);

        service.completeHabit(1L);

        verify(completionRepository).save(
                org.mockito.ArgumentMatchers.any(HabitCompletion.class)
        );
    }

    @Test
    void shouldNotCompleteHabitTwiceInOneDay() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L))
                .thenReturn(java.util.Optional.of(habit));

        when(completionRepository.existsByHabitIdAndCompletionDate(
                1L,
                LocalDate.of(2026, 8, 14)
        )).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.completeHabit(1L)
        );

        org.mockito.Mockito.verify(
                completionRepository,
                org.mockito.Mockito.never()
        ).save(org.mockito.ArgumentMatchers.any(HabitCompletion.class));
    }

    @Test
    void shouldThrowExceptionWhenHabitDoesNotExist() {

        when(habitRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> service.completeHabit(999L)
        );

        verify(
                completionRepository,
                org.mockito.Mockito.never()
        ).save(org.mockito.ArgumentMatchers.any(HabitCompletion.class));
    }
}