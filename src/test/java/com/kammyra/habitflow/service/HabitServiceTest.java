package com.kammyra.habitflow.service;

import com.kammyra.habitflow.dto.HabitRequest;
import com.kammyra.habitflow.dto.HabitUpdateRequest;
import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.enums.Frequency;
import com.kammyra.habitflow.exception.HabitFrequencyChangeException;
import com.kammyra.habitflow.exception.HabitNotFoundException;
import com.kammyra.habitflow.repository.HabitCompletionRepository;
import com.kammyra.habitflow.repository.HabitRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HabitServiceTest {

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final HabitCompletionRepository habitCompletionRepository = mock(HabitCompletionRepository.class);

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);
    private final HabitService service =
            new HabitService(
                    habitRepository,
                    habitCompletionRepository,
                    clock
            );

    @Test
    void shouldCreateHabitSuccessfully() {

        HabitRequest request = new HabitRequest();
        request.setName("Чтение");
        request.setDescription("Читать каждый день");
        request.setFrequency(Frequency.DAILY);

        Habit savedHabit = new Habit(
                "Чтение",
                "Читать каждый день",
                Frequency.DAILY,
                null
        );

        savedHabit.setId(1L);

        when(habitRepository.save(any(Habit.class)))
                .thenReturn(savedHabit);

        var result = service.createHabit(request);

        assertEquals(1L, result.getId());
        assertEquals("Чтение", result.getName());
        assertEquals("Читать каждый день", result.getDescription());
        assertEquals(Frequency.DAILY, result.getFrequency());

        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    void shouldReturnHabitById() {

        Habit habit = new Habit(
                "Чтение",
                "Читать каждый день",
                Frequency.DAILY,
                null
        );

        habit.setId(1L);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        var result = service.getHabitById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Чтение", result.getName());
        assertEquals(Frequency.DAILY, result.getFrequency());

        verify(habitRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenHabitDoesNotExist() {
        when(habitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(HabitNotFoundException.class, () -> service.getHabitById(999L));

        verify(habitRepository).findById(999L);
    }

    @Test
    void shouldReturnAllHabits() {

        Habit firstHabit = new Habit(
                "Чтение",
                "Читать каждый день",
                Frequency.DAILY,
                null
        );

        firstHabit.setId(1L);

        Habit secondHabit = new Habit(
                "Тренировка",
                "Заниматься спортом",
                Frequency.WEEKLY,
                null
        );

        secondHabit.setId(2L);

        when(habitRepository.findAll())
                .thenReturn(List.of(firstHabit, secondHabit));

        var result = service.getAllHabits();

        assertEquals(2, result.size());

        assertEquals("Чтение", result.get(0).getName());
        assertEquals("Тренировка", result.get(1).getName());

        verify(habitRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoHabits() {

        when(habitRepository.findAll())
                .thenReturn(List.of());

        var result = service.getAllHabits();

        assertTrue(result.isEmpty());

        verify(habitRepository).findAll();
    }

    @Test
    void shouldUpdateHabitSuccessfully() {

        Habit habit = new Habit(
                "Чтение",
                "Старая версия",
                Frequency.DAILY,
                null
        );

        habit.setId(1L);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(habitRepository.save(habit))
                .thenReturn(habit);

        HabitUpdateRequest request = new HabitUpdateRequest();
        request.setName("Чтение книг");
        request.setDescription("Читать 30 минут каждый день");
        request.setFrequency(Frequency.DAILY);

        var result = service.updateHabit(1L, request);

        assertEquals("Чтение книг", result.getName());
        assertEquals(
                "Читать 30 минут каждый день",
                result.getDescription()
        );
        assertEquals(Frequency.DAILY, result.getFrequency());

        verify(habitRepository).findById(1L);
        verify(habitRepository).save(habit);
    }

    @Test
    void shouldAllowFrequencyChangeWhenHabitHasNoCompletions() {

        Habit habit = new Habit(
                "Чтение",
                "Читать каждый день",
                Frequency.DAILY,
                null
        );

        habit.setId(1L);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(habitCompletionRepository.existsByHabitId(1L))
                .thenReturn(false);

        when(habitRepository.save(habit))
                .thenReturn(habit);

        HabitUpdateRequest request = new HabitUpdateRequest();
        request.setName("Чтение");
        request.setDescription("Читать каждый день");
        request.setFrequency(Frequency.WEEKLY);

        var result = service.updateHabit(1L, request);

        assertEquals(Frequency.WEEKLY, result.getFrequency());

        verify(habitCompletionRepository)
                .existsByHabitId(1L);

        verify(habitRepository)
                .save(habit);
    }

    @Test
    void shouldThrowExceptionWhenChangingFrequencyAfterCompletion() {

        Habit habit = new Habit(
                "Чтение",
                "Читать каждый день",
                Frequency.DAILY,
                null
        );

        habit.setId(1L);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        when(habitCompletionRepository.existsByHabitId(1L))
                .thenReturn(true);

        HabitUpdateRequest request = new HabitUpdateRequest();
        request.setName("Чтение");
        request.setDescription("Читать каждый день");
        request.setFrequency(Frequency.WEEKLY);

        assertThrows(
                HabitFrequencyChangeException.class,
                () -> service.updateHabit(1L, request)
        );

        verify(habitCompletionRepository)
                .existsByHabitId(1L);

        verify(habitRepository, never())
                .save(any(Habit.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingHabit() {

        when(habitRepository.findById(999L))
                .thenReturn(Optional.empty());

        HabitUpdateRequest request = new HabitUpdateRequest();
        request.setName("Чтение");
        request.setDescription("Описание");
        request.setFrequency(Frequency.DAILY);

        assertThrows(
                HabitNotFoundException.class,
                () -> service.updateHabit(999L, request)
        );

        verify(habitRepository, never())
                .save(any(Habit.class));
    }

    @Test
    void shouldDeleteHabitSuccessfully() {

        Habit habit = new Habit(
                "Чтение",
                "Читать каждый день",
                Frequency.DAILY,
                null
        );

        habit.setId(1L);

        when(habitRepository.findById(1L))
                .thenReturn(Optional.of(habit));

        service.deleteHabit(1L);

        verify(habitRepository).findById(1L);
        verify(habitRepository).delete(habit);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingHabit() {
        when(habitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(HabitNotFoundException.class, () -> service.deleteHabit(999L));

        verify(habitRepository, never()).delete(any(Habit.class));
    }
}