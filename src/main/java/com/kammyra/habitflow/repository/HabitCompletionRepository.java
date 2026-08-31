package com.kammyra.habitflow.repository;

import com.kammyra.habitflow.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    List<HabitCompletion> findByHabitId(Long habitId);

    List<HabitCompletion> findByHabitIdOrderByCompletedAtAsc(Long habitId);

    boolean existsByHabitIdAndCompletionDate(
            Long habitId,
            LocalDate completionDate
    );

    boolean existsByHabitIdAndCompletionDateBetween(
            Long habitId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT c FROM HabitCompletion c JOIN FETCH c.habit WHERE c.habit.id = :habitId")
    List<HabitCompletion> findByHabitIdWithHabit(@Param("habitId") Long habitId);

    List<HabitCompletion> findByHabitIdOrderByCompletionDateAsc(Long habitId);
}