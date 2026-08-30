package com.kammyra.habitflow.controller;

import tools.jackson.databind.ObjectMapper;
import com.kammyra.habitflow.config.TimeConfig;
import com.kammyra.habitflow.dto.HabitCompletionRequest;
import com.kammyra.habitflow.dto.HabitCompletionResponse;
import com.kammyra.habitflow.dto.HabitStatisticsResponse;
import com.kammyra.habitflow.exception.HabitAlreadyCompletedException;
import com.kammyra.habitflow.exception.HabitNotFoundException;
import com.kammyra.habitflow.service.HabitCompletionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HabitCompletionController.class)
@Import(TimeConfig.class)
class HabitCompletionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HabitCompletionService completionService;


    @Test
    void shouldCreateCompletion() throws Exception {

        Long habitId = 1L;

        HabitCompletionRequest request = new HabitCompletionRequest();
        request.setCompletionDate(LocalDate.of(2026, 8, 22));

        HabitCompletionResponse response = new HabitCompletionResponse(
                1L,
                habitId,
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 22, 10, 0)
        );

        when(completionService.createCompletion(
                eq(habitId),
                any(HabitCompletionRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.habitId").value(1))
                .andExpect(jsonPath("$.completionDate").value("2026-08-22"))
                .andExpect(jsonPath("$.completedAt").value("2026-08-22T10:00:00"));

        verify(completionService).createCompletion(
                eq(habitId),
                any(HabitCompletionRequest.class)
        );
    }

    @Test
    void shouldReturnCompletions() throws Exception {

        Long habitId = 1L;

        List<HabitCompletionResponse> completions = List.of(
                new HabitCompletionResponse(1L, habitId, LocalDate.of(2026, 8, 20), LocalDateTime.of(2026, 8, 20, 10, 0)),
                new HabitCompletionResponse(2L, habitId, LocalDate.of(2026, 8, 21), LocalDateTime.of(2026, 8, 21, 10, 0))
        );

        when(completionService.getCompletions(habitId)).thenReturn(completions);

        mockMvc.perform(get("/api/habits/{habitId}/completions", habitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(completionService).getCompletions(habitId);
    }


    @Test
    void shouldReturnStatistics() throws Exception {

        Long habitId = 1L;

        HabitStatisticsResponse response =
                new HabitStatisticsResponse(
                        habitId,
                        "Чтение",
                        10L,
                        5,
                        7,
                        LocalDate.of(2026, 8, 22)
                );

        when(completionService.getStatistics(habitId)).thenReturn(response);

        mockMvc.perform(get("/api/habits/{habitId}/completions/statistics", habitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habitId").value(1))
                .andExpect(jsonPath("$.habitName").value("Чтение"))
                .andExpect(jsonPath("$.totalCompletions").value(10))
                .andExpect(jsonPath("$.currentStreak").value(5))
                .andExpect(jsonPath("$.bestStreak").value(7))
                .andExpect(jsonPath("$.lastCompletedDate").value("2026-08-22"));

        verify(completionService).getStatistics(habitId);
    }


    @Test
    void shouldReturn404WhenHabitNotFound() throws Exception {

        Long habitId = 999L;

        when(completionService.getCompletions(habitId)).thenThrow(new HabitNotFoundException(habitId));

        mockMvc.perform(get("/api/habits/{habitId}/completions", habitId))
                .andExpect(status().isNotFound());

        verify(completionService).getCompletions(habitId);
    }


    @Test
    void shouldReturn409WhenCompletionAlreadyExists() throws Exception {

        Long habitId = 1L;

        LocalDate validDate = LocalDate.of(2026, 8, 27);
        HabitCompletionRequest request = new HabitCompletionRequest();
        request.setCompletionDate(validDate);

        when(completionService.createCompletion(eq(habitId), any(HabitCompletionRequest.class)))
                .thenThrow(new HabitAlreadyCompletedException(habitId, validDate));

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(completionService).createCompletion(
                eq(habitId),
                any(HabitCompletionRequest.class)
        );
    }


    @Test
    void shouldReturn400WhenCompletionDateIsInFuture() throws Exception {

        Long habitId = 1L;

        HabitCompletionRequest request = new HabitCompletionRequest();
        request.setCompletionDate(LocalDate.of(2030, 1, 1));

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(completionService, never()).createCompletion(eq(habitId), any(HabitCompletionRequest.class));
    }
}