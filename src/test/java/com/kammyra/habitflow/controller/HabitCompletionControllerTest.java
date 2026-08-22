package com.kammyra.habitflow.controller;

import tools.jackson.databind.ObjectMapper;
import com.kammyra.habitflow.dto.HabitCompletionRequest;
import com.kammyra.habitflow.dto.HabitCompletionResponse;
import com.kammyra.habitflow.dto.HabitStatisticsResponse;
import com.kammyra.habitflow.exception.FutureCompletionException;
import com.kammyra.habitflow.exception.HabitAlreadyCompletedException;
import com.kammyra.habitflow.exception.HabitNotFoundException;
import com.kammyra.habitflow.service.HabitCompletionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitCompletionController.class)
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

        request.setCompletedAt(
                LocalDateTime.of(2026, 8, 22, 10, 0)
        );

        HabitCompletionResponse response = new HabitCompletionResponse(
                        1L,
                        habitId,
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
                .andExpect(jsonPath("$.completedAt")
                        .value("2026-08-22T10:00:00"));

        verify(completionService).createCompletion(
                eq(habitId),
                any(HabitCompletionRequest.class)
        );
    }


    @Test
    void shouldCreateCompletionWithoutRequestBody() throws Exception {

        Long habitId = 1L;

        HabitCompletionResponse response =
                new HabitCompletionResponse(
                        1L,
                        habitId,
                        LocalDateTime.of(2026, 8, 22, 10, 0)
                );

        when(completionService.createCompletion(
                eq(habitId),
                isNull()
        )).thenReturn(response);

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.habitId").value(1));

        verify(completionService).createCompletion(
                eq(habitId),
                isNull()
        );
    }


    @Test
    void shouldReturnCompletions() throws Exception {

        Long habitId = 1L;

        List<HabitCompletionResponse> completions = List.of(
                new HabitCompletionResponse(
                        1L,
                        habitId,
                        LocalDateTime.of(2026, 8, 20, 10, 0)
                ),
                new HabitCompletionResponse(
                        2L,
                        habitId,
                        LocalDateTime.of(2026, 8, 21, 10, 0)
                )
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
                .andExpect(jsonPath("$.lastCompletedDate")
                        .value("2026-08-22"));

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

        HabitCompletionRequest request = new HabitCompletionRequest();

        request.setCompletedAt(
                LocalDateTime.of(2026, 8, 22, 10, 0)
        );

        when(completionService.createCompletion(
                eq(habitId),
                any(HabitCompletionRequest.class)
        )).thenThrow(
                new HabitAlreadyCompletedException(
                        habitId,
                        LocalDate.of(2026, 8, 22)
                )
        );

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

        request.setCompletedAt(
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        when(completionService.createCompletion(
                eq(habitId),
                any(HabitCompletionRequest.class)
        )).thenThrow(
                new FutureCompletionException(request.getCompletedAt())
        );

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(completionService).createCompletion(
                eq(habitId),
                any(HabitCompletionRequest.class)
        );
    }
}