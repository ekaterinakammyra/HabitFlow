package com.kammyra.habitflow.service;

import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.entity.HabitCompletion;
import com.kammyra.habitflow.enums.Frequency;
import com.kammyra.habitflow.repository.HabitCompletionRepository;
import com.kammyra.habitflow.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HabitCompletionServiceTest {

    @Mock
    private HabitCompletionRepository completionRepository;

    @Mock
    private HabitRepository habitRepository;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-08-14T10:00:00Z"),
            ZoneId.of("UTC")
    );

    private HabitCompletionService service;

    @BeforeEach
    void setUp() {
        service = new HabitCompletionService(completionRepository, habitRepository, clock);
    }

    @Test
    void shouldReturnZeroStreakWhenThereAreNoCompletions() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L)).thenReturn(List.of());

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

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
                .thenReturn(List.of(completion(LocalDate.of(2026, 8, 14))));

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

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
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

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
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

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
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

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
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

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
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

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 7, 20)),
                        completion(LocalDate.of(2026, 8, 3)),
                        completion(LocalDate.of(2026, 8, 10))
                ));

        var result = service.getStatistics(1L);

        assertEquals(2, result.getCurrentStreak());
        assertEquals(2, result.getBestStreak());
        assertEquals(3, result.getTotalCompletions());
    }

    @Test
    void shouldCompleteHabitSuccessfully() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(completionRepository.existsByHabitIdAndCompletionDate(1L, LocalDate.of(2026, 8, 14))).thenReturn(false);
        when(completionRepository.save(any(HabitCompletion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.completeHabit(1L);

        verify(completionRepository).save(any(HabitCompletion.class));
    }

    @Test
    void shouldNotCompleteHabitTwiceInOneDay() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(completionRepository.existsByHabitIdAndCompletionDate(1L, LocalDate.of(2026, 8, 14))).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.completeHabit(1L));

        verify(completionRepository, never()).save(any(HabitCompletion.class));
    }

    @Test
    void shouldThrowExceptionWhenHabitDoesNotExist() {
        when(habitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.completeHabit(999L));

        verify(completionRepository, never()).save(any(HabitCompletion.class));
    }
}