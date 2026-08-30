package com.kammyra.habitflow.controller;

import com.kammyra.habitflow.dto.HabitRequest;
import com.kammyra.habitflow.dto.HabitResponse;
import com.kammyra.habitflow.dto.HabitUpdateRequest;
import com.kammyra.habitflow.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse createHabit(@Valid @RequestBody HabitRequest request) {
        return habitService.createHabit(request);
    }

    @GetMapping
    public List<HabitResponse> getAllHabits() {
        return habitService.getAllHabits();
    }

    @GetMapping("/{id}")
    public HabitResponse getHabitById(@PathVariable Long id) {
        return habitService.getHabitById(id);
    }

    @PutMapping("/{id}")
    public HabitResponse updateHabit(
            @PathVariable Long id,
            @Valid @RequestBody HabitUpdateRequest request
    ) {
        return habitService.updateHabit(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
    }
}