package com.kammyra.habitflow.controller;

import com.kammyra.habitflow.dto.HabitCompletionRequest;
import com.kammyra.habitflow.dto.HabitCompletionResponse;
import com.kammyra.habitflow.dto.HabitStatisticsResponse;
import com.kammyra.habitflow.service.HabitCompletionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits/{habitId}/completions")
public class HabitCompletionController {

    private final HabitCompletionService completionService;

    public HabitCompletionController(HabitCompletionService completionService) {
        this.completionService = completionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitCompletionResponse createCompletion(
            @PathVariable Long habitId,
            @RequestBody(required = false) HabitCompletionRequest request
    ) {
        return completionService.createCompletion(habitId, request);
    }

    @GetMapping
    public List<HabitCompletionResponse> getCompletions(
            @PathVariable Long habitId
    ) {
        return completionService.getCompletions(habitId);
    }

    @GetMapping("/statistics")
    public HabitStatisticsResponse getStatistics(
            @PathVariable Long habitId
    ) {
        return completionService.getStatistics(habitId);
    }
}