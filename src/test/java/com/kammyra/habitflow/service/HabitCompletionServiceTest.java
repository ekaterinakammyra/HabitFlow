package com.kammyra.habitflow.service;

import com.kammyra.habitflow.dto.HabitCompletionRequest;
import com.kammyra.habitflow.dto.HabitCompletionResponse;
import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.entity.HabitCompletion;
import com.kammyra.habitflow.enums.Frequency;
import com.kammyra.habitflow.exception.CompletionBeforeHabitCreationException;
import com.kammyra.habitflow.exception.FutureCompletionException;
import com.kammyra.habitflow.exception.HabitAlreadyCompletedException;
import com.kammyra.habitflow.repository.HabitCompletionRepository;
import com.kammyra.habitflow.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        habit.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        8,
                        10,
                        10,
                        0
                )
        );

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository
                .existsByHabitIdAndCompletionDate(
                        1L,
                        LocalDate.of(2026, 8, 14)
                ))
                .thenReturn(false);

        when(completionRepository.save(any(HabitCompletion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.completeHabit(1L);

        verify(completionRepository).save(any(HabitCompletion.class));
    }

    @Test
    void shouldNotCompleteHabitTwiceInOneDay() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);
        habit.setCreatedAt(
                LocalDateTime.of(2026, 8, 10, 10, 0)
        );

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository
                .existsByHabitIdAndCompletionDate(
                        1L,
                        LocalDate.of(2026, 8, 14)
                ))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.completeHabit(1L)
        );

        verify(completionRepository, never())
                .save(any(HabitCompletion.class));
    }

    @Test
    void shouldThrowExceptionWhenHabitDoesNotExist() {
        when(habitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.completeHabit(999L));

        verify(completionRepository, never()).save(any(HabitCompletion.class));
    }

    @Test
    void shouldNotCompleteWeeklyHabitTwiceInSameWeek() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setFrequency(Frequency.WEEKLY);
        habit.setCreatedAt(LocalDateTime.of(2026, 8, 10, 10, 0));

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository
                .existsByHabitIdAndCompletionDateBetween(
                        eq(1L),
                        eq(LocalDate.of(2026, 8, 10)),
                        eq(LocalDate.of(2026, 8, 16))
                ))
                .thenReturn(true);

        HabitCompletionRequest request = new HabitCompletionRequest();
        request.setCompletionDate(LocalDate.of(2026, 8, 14));

        assertThrows(
                HabitAlreadyCompletedException.class,
                () -> service.createCompletion(1L, request)
        );

        verify(completionRepository, never())
                .save(any(HabitCompletion.class));
    }

    @Test
    void shouldAllowWeeklyHabitCompletionInNextWeek() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setFrequency(Frequency.WEEKLY);
        habit.setCreatedAt(LocalDateTime.of(2026, 8, 1, 10, 0));

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository
                .existsByHabitIdAndCompletionDateBetween(
                        eq(1L),
                        eq(LocalDate.of(2026, 8, 3)),
                        eq(LocalDate.of(2026, 8, 9))
                ))
                .thenReturn(false);

        when(completionRepository.save(any(HabitCompletion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HabitCompletionRequest request = new HabitCompletionRequest();
        request.setCompletionDate(LocalDate.of(2026, 8, 3));

        HabitCompletionResponse response =
                service.createCompletion(1L, request);

        assertNotNull(response);

        verify(completionRepository)
                .save(any(HabitCompletion.class));
    }

    @Test
    void shouldNotCompleteHabitBeforeHabitCreationDate() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);
        habit.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        8,
                        10,
                        10,
                        0
                )
        );

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        HabitCompletionRequest request =
                new HabitCompletionRequest();

        request.setCompletionDate(
                LocalDate.of(2026, 8, 9)
        );

        assertThrows(
                CompletionBeforeHabitCreationException.class,
                () -> service.createCompletion(1L, request)
        );

        verify(completionRepository, never())
                .save(any(HabitCompletion.class));
    }

    @Test
    void shouldAllowCompletionOnHabitCreationDate() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);
        habit.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        8,
                        10,
                        10,
                        0
                )
        );

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository
                .existsByHabitIdAndCompletionDate(
                        1L,
                        LocalDate.of(2026, 8, 10)
                ))
                .thenReturn(false);

        when(completionRepository.save(any(HabitCompletion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HabitCompletionRequest request =
                new HabitCompletionRequest();

        request.setCompletionDate(
                LocalDate.of(2026, 8, 10)
        );

        HabitCompletionResponse response =
                service.createCompletion(1L, request);

        assertNotNull(response);

        verify(completionRepository)
                .save(any(HabitCompletion.class));
    }

    @Test
    void shouldNotCompleteHabitInFuture() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);
        habit.setCreatedAt(
                LocalDateTime.of(2026, 8, 10, 10, 0)
        );

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        HabitCompletionRequest request =
                new HabitCompletionRequest();

        request.setCompletionDate(
                LocalDate.of(2026, 8, 15)
        );

        assertThrows(
                FutureCompletionException.class,
                () -> service.createCompletion(1L, request)
        );

        verify(completionRepository, never())
                .save(any(HabitCompletion.class));
    }

    @Test
    void shouldAllowCompletionYesterday() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);
        habit.setCreatedAt(
                LocalDateTime.of(2026, 8, 10, 10, 0)
        );

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository
                .existsByHabitIdAndCompletionDate(
                        1L,
                        LocalDate.of(2026, 8, 13)
                ))
                .thenReturn(false);

        when(completionRepository.save(any(HabitCompletion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HabitCompletionRequest request =
                new HabitCompletionRequest();

        request.setCompletionDate(
                LocalDate.of(2026, 8, 13)
        );

        HabitCompletionResponse response =
                service.createCompletion(1L, request);

        assertNotNull(response);
        assertEquals(
                LocalDate.of(2026, 8, 13),
                response.getCompletionDate()
        );

        verify(completionRepository)
                .save(any(HabitCompletion.class));
    }

    @Test
    void shouldUseTodayWhenRequestIsNull() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);
        habit.setCreatedAt(
                LocalDateTime.of(2026, 8, 10, 10, 0)
        );

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository
                .existsByHabitIdAndCompletionDate(
                        1L,
                        LocalDate.of(2026, 8, 14)
                ))
                .thenReturn(false);

        when(completionRepository.save(any(HabitCompletion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HabitCompletionResponse response =
                service.createCompletion(1L, null);

        assertNotNull(response);

        assertEquals(
                LocalDate.of(2026, 8, 14),
                response.getCompletionDate()
        );

        verify(completionRepository)
                .save(any(HabitCompletion.class));
    }

    @Test
    void shouldUseTodayWhenCompletionDateIsNull() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);
        habit.setCreatedAt(
                LocalDateTime.of(2026, 8, 10, 10, 0)
        );

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository
                .existsByHabitIdAndCompletionDate(
                        1L,
                        LocalDate.of(2026, 8, 14)
                ))
                .thenReturn(false);

        when(completionRepository.save(any(HabitCompletion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HabitCompletionRequest request =
                new HabitCompletionRequest();

        request.setCompletionDate(null);

        HabitCompletionResponse response =
                service.createCompletion(1L, request);

        assertNotNull(response);

        assertEquals(
                LocalDate.of(2026, 8, 14),
                response.getCompletionDate()
        );

        verify(completionRepository)
                .save(any(HabitCompletion.class));
    }

    @Test
    void shouldNotSaveCompletionWhenDateIsInFuture() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Чтение");
        habit.setFrequency(Frequency.DAILY);
        habit.setCreatedAt(
                LocalDateTime.of(2026, 8, 10, 10, 0)
        );

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        HabitCompletionRequest request =
                new HabitCompletionRequest();

        request.setCompletionDate(
                LocalDate.of(2026, 8, 20)
        );

        assertThrows(
                FutureCompletionException.class,
                () -> service.createCompletion(1L, request)
        );

        verify(completionRepository, never())
                .save(any(HabitCompletion.class));

        verify(completionRepository, never())
                .existsByHabitIdAndCompletionDate(
                        anyLong(),
                        any(LocalDate.class)
                );
    }

    @Test
    void shouldCountSameWeekOnlyOnceForWeeklyStreak() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Тренировка");
        habit.setFrequency(Frequency.WEEKLY);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 8, 10)),
                        completion(LocalDate.of(2026, 8, 14))
                ));

        var result = service.getStatistics(1L);

        assertEquals(1, result.getCurrentStreak());
        assertEquals(1, result.getBestStreak());
        assertEquals(2, result.getTotalCompletions());
    }

    @Test
    void shouldCountConsecutiveWeeklyCompletionsEvenOnDifferentDays() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Тренировка");
        habit.setFrequency(Frequency.WEEKLY);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 8, 4)),
                        completion(LocalDate.of(2026, 8, 10)),
                        completion(LocalDate.of(2026, 8, 14))
                ));

        var result = service.getStatistics(1L);

        assertEquals(2, result.getCurrentStreak());
        assertEquals(2, result.getBestStreak());
        assertEquals(3, result.getTotalCompletions());
    }

    @Test
    void shouldCalculateBestWeeklyStreakBeforeGap() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Тренировка");
        habit.setFrequency(Frequency.WEEKLY);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 7, 13)),
                        completion(LocalDate.of(2026, 7, 20)),
                        completion(LocalDate.of(2026, 7, 27)),
                        completion(LocalDate.of(2026, 8, 10))
                ));

        var result = service.getStatistics(1L);

        assertEquals(1, result.getCurrentStreak());
        assertEquals(3, result.getBestStreak());
        assertEquals(4, result.getTotalCompletions());
    }

    @Test
    void shouldReturnZeroWeeklyCurrentStreakAfterLongGap() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Тренировка");
        habit.setFrequency(Frequency.WEEKLY);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 7, 20))
                ));

        var result = service.getStatistics(1L);

        assertEquals(0, result.getCurrentStreak());
        assertEquals(1, result.getBestStreak());
    }

    @Test
    void shouldKeepWeeklyStreakWhenWeekContainsMultipleCompletions() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Тренировка");
        habit.setFrequency(Frequency.WEEKLY);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 7, 27)),
                        completion(LocalDate.of(2026, 7, 29)),
                        completion(LocalDate.of(2026, 8, 3)),
                        completion(LocalDate.of(2026, 8, 10))
                ));

        var result = service.getStatistics(1L);

        assertEquals(3, result.getCurrentStreak());
        assertEquals(3, result.getBestStreak());
        assertEquals(4, result.getTotalCompletions());
    }

    @Test
    void shouldReturnWeeklyCurrentStreakWhenCompletedToday() {

        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Тренировка");
        habit.setFrequency(Frequency.WEEKLY);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(completionRepository.findByHabitIdOrderByCompletionDateAsc(1L))
                .thenReturn(List.of(
                        completion(LocalDate.of(2026, 8, 3)),
                        completion(LocalDate.of(2026, 8, 10)),
                        completion(LocalDate.of(2026, 8, 14))
                ));

        var result = service.getStatistics(1L);

        assertEquals(2, result.getCurrentStreak());
        assertEquals(2, result.getBestStreak());
    }
}