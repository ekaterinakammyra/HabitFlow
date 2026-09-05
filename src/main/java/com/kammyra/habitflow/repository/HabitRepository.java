package com.kammyra.habitflow.repository;

import com.kammyra.habitflow.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<Habit, Long> {
}