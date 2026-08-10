package com.kammyra.habitflow.controller;

import com.kammyra.habitflow.dto.HabitRequest;
import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @PostMapping
    public Habit createHabit(@Valid @RequestBody HabitRequest request) {
        return habitService.createHabit(request);
    }

    @GetMapping
    public List<Habit> getAllHabits() {
        return habitService.getAllHabits();
    }

    @GetMapping("/{id}")
    public Habit getHabitById(@PathVariable Long id) {
        return habitService.getHabitById(id);
    }

    @PutMapping("/{id}")
    public Habit updateHabit(
            @PathVariable Long id,
            @RequestBody Habit habit
    ) {
        return habitService.updateHabit(id, habit);
    }

    @DeleteMapping("/{id}")
    public void deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
    }
}