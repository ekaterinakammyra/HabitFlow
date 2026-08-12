package com.kammyra.habitflow.repository;

import com.kammyra.habitflow.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    List<HabitCompletion> findByHabitId(Long habitId);

    List<HabitCompletion> findByHabitIdOrderByCompletedAtAsc(Long habitId);

    boolean existsByHabitIdAndCompletionDate(
            Long habitId,
            LocalDate completionDate
    );
}