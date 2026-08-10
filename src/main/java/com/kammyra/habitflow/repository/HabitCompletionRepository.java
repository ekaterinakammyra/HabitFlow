package com.kammyra.habitflow.repository;

import com.kammyra.habitflow.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    List<HabitCompletion> findByHabitId(Long habitId);

    List<HabitCompletion> findByHabitIdOrderByCompletedAtAsc(Long habitId);
}