package com.kammyra.habitflow.repository;

import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.enums.Frequency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HabitRepositoryTest {

    @Autowired
    private HabitRepository habitRepository;

    @Test
    void shouldSaveAndFindHabit() {
        Habit habit = new Habit();
        habit.setName("Чтение");
        habit.setDescription("Прочитать 30 страниц");
        habit.setFrequency(Frequency.DAILY);
        habit.setCreatedAt(LocalDateTime.now());

        Habit saved = habitRepository.save(habit);

        Optional<Habit> found = habitRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Чтение");
        assertThat(found.get().getDescription())
                .isEqualTo("Прочитать 30 страниц");
        assertThat(found.get().getFrequency())
                .isEqualTo(Frequency.DAILY);
    }

    @Test
    void shouldDeleteHabit() {
        Habit habit = new Habit();
        habit.setName("Тренировка");
        habit.setDescription("Тренировка 3 раза");
        habit.setFrequency(Frequency.WEEKLY);
        habit.setCreatedAt(LocalDateTime.now());

        Habit saved = habitRepository.save(habit);

        habitRepository.deleteById(saved.getId());

        Optional<Habit> found = habitRepository.findById(saved.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnAllHabits() {
        Habit first = new Habit();
        first.setName("Чтение");
        first.setDescription("Прочитать 30 страниц");
        first.setFrequency(Frequency.DAILY);
        first.setCreatedAt(LocalDateTime.now());

        Habit second = new Habit();
        second.setName("Тренировка");
        second.setDescription("Тренировка 3 раза");
        second.setFrequency(Frequency.WEEKLY);
        second.setCreatedAt(LocalDateTime.now());

        habitRepository.save(first);
        habitRepository.save(second);

        var habits = habitRepository.findAll();

        assertThat(habits).hasSizeGreaterThanOrEqualTo(2);
        assertThat(habits)
                .extracting(Habit::getName)
                .contains("Чтение", "Тренировка");
    }
}