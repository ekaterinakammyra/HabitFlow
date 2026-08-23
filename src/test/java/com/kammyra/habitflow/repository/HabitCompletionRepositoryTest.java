package com.kammyra.habitflow.repository;

import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.entity.HabitCompletion;
import com.kammyra.habitflow.enums.Frequency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HabitCompletionRepositoryTest {

    @Autowired
    private HabitCompletionRepository completionRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Test
    void shouldFindCompletionsByHabitId() {

        Habit habit = createHabit("Пить воду");

        HabitCompletion first = createCompletion(
                habit,
                LocalDateTime.of(2026, 8, 20, 10, 0)
        );

        HabitCompletion second = createCompletion(
                habit,
                LocalDateTime.of(2026, 8, 21, 10, 0)
        );

        completionRepository.save(first);
        completionRepository.save(second);

        List<HabitCompletion> result = completionRepository.findByHabitId(habit.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream()
                        .allMatch(completion ->
                                completion.getHabit().getId()
                                        .equals(habit.getId()))
        );
    }

    @Test
    void shouldFindCompletionsOrderedByCompletedAtAscending() {

        Habit habit = createHabit("Чтение");

        HabitCompletion latest = createCompletion(
                habit,
                LocalDateTime.of(2026, 8, 22, 10, 0)
        );

        HabitCompletion earliest = createCompletion(
                habit,
                LocalDateTime.of(2026, 8, 20, 10, 0)
        );

        HabitCompletion middle = createCompletion(
                habit,
                LocalDateTime.of(2026, 8, 21, 10, 0)
        );

        completionRepository.save(latest);
        completionRepository.save(earliest);
        completionRepository.save(middle);

        List<HabitCompletion> result = completionRepository.findByHabitIdOrderByCompletedAtAsc(habit.getId());

        assertEquals(3, result.size());

        assertEquals(
                LocalDateTime.of(2026, 8, 20, 10, 0),
                result.get(0).getCompletedAt()
        );

        assertEquals(
                LocalDateTime.of(2026, 8, 21, 10, 0),
                result.get(1).getCompletedAt()
        );

        assertEquals(
                LocalDateTime.of(2026, 8, 22, 10, 0),
                result.get(2).getCompletedAt()
        );
    }

    @Test
    void shouldReturnTrueWhenCompletionExistsForHabitAndDate() {

        Habit habit = createHabit("Чтение");

        HabitCompletion completion = createCompletion(
                habit,
                LocalDateTime.of(2026, 8, 22, 18, 30)
        );

        completionRepository.save(completion);

        boolean result = completionRepository.existsByHabitIdAndCompletionDate(
                                habit.getId(),
                                LocalDate.of(2026, 8, 22)
        );

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenCompletionDoesNotExistForHabitAndDate() {

        Habit habit = createHabit("Чтение");

        boolean result = completionRepository.existsByHabitIdAndCompletionDate(
                                habit.getId(),
                                LocalDate.of(2026, 8, 22)
        );

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenCompletionExistsForAnotherDate() {

        Habit habit = createHabit("Пить воду");

        HabitCompletion completion = createCompletion(
                habit,
                LocalDateTime.of(2026, 8, 21, 12, 0)
        );

        completionRepository.save(completion);

        boolean result = completionRepository.existsByHabitIdAndCompletionDate(
                                habit.getId(),
                                LocalDate.of(2026, 8, 22)
        );

        assertFalse(result);
    }

    @Test
    void shouldNotReturnCompletionsFromAnotherHabit() {

        Habit firstHabit = createHabit("Чтение");
        Habit secondHabit = createHabit("Пить воду");

        completionRepository.save(createCompletion(
                        firstHabit,
                        LocalDateTime.of(2026, 8, 22, 10, 0))
        );

        completionRepository.save(createCompletion(
                        secondHabit,
                        LocalDateTime.of(2026, 8, 22, 11, 0))
        );

        List<HabitCompletion> result = completionRepository.findByHabitId(firstHabit.getId());

        assertEquals(1, result.size());

        assertEquals(
                firstHabit.getId(),
                result.get(0).getHabit().getId()
        );
    }

    private Habit createHabit(String name) {

        Habit habit = new Habit();

        habit.setName(name);
        habit.setDescription("Чтение");
        habit.setFrequency(Frequency.DAILY);

        return habitRepository.save(habit);
    }

    private HabitCompletion createCompletion(
            Habit habit,
            LocalDateTime completedAt
    ) {

        HabitCompletion completion = new HabitCompletion();

        completion.setHabit(habit);
        completion.setCompletedAt(completedAt);
        completion.setCompletionDate(completedAt.toLocalDate());

        return completion;
    }
}