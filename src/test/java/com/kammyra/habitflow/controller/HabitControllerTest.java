package com.kammyra.habitflow.controller;

import com.kammyra.habitflow.config.TimeConfig;
import com.kammyra.habitflow.dto.HabitRequest;
import com.kammyra.habitflow.dto.HabitResponse;
import com.kammyra.habitflow.dto.HabitUpdateRequest;
import com.kammyra.habitflow.enums.Frequency;
import com.kammyra.habitflow.exception.HabitFrequencyChangeException;
import com.kammyra.habitflow.service.HabitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitController.class)
@Import(TimeConfig.class)
class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HabitService habitService;


    @Test
    void shouldCreateHabit() throws Exception {

        HabitRequest request = new HabitRequest();

        request.setName("Чтение");
        request.setDescription("Читать каждый день");
        request.setFrequency(Frequency.DAILY);

        HabitResponse response = new HabitResponse(
                1L,
                "Чтение",
                "Читать каждый день",
                Frequency.DAILY,
                LocalDateTime.of(2026, 8, 14, 10, 0)
        );

        when(habitService.createHabit(any(HabitRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Чтение"))
                .andExpect(jsonPath("$.description").value("Читать каждый день"))
                .andExpect(jsonPath("$.frequency").value("DAILY"));

        verify(habitService).createHabit(any(HabitRequest.class));
    }


    @Test
    void shouldReturnAllHabits() throws Exception {

        HabitResponse firstHabit = new HabitResponse(
                1L,
                "Чтение",
                "Читать каждый день",
                Frequency.DAILY,
                LocalDateTime.of(2026, 8, 14, 10, 0)
        );

        HabitResponse secondHabit = new HabitResponse(
                2L,
                "Тренировка",
                "Тренироваться три раза в неделю",
                Frequency.WEEKLY,
                LocalDateTime.of(2026, 8, 14, 11, 0)
        );

        when(habitService.getAllHabits()).thenReturn(List.of(firstHabit, secondHabit));

        mockMvc.perform(
                        get("/api/habits")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Чтение"))
                .andExpect(jsonPath("$[1].name").value("Тренировка"));

        verify(habitService).getAllHabits();
    }


    @Test
    void shouldReturnHabitById() throws Exception {

        HabitResponse response = new HabitResponse(
                1L,
                "Чтение",
                "Читать каждый день",
                Frequency.DAILY,
                LocalDateTime.of(2026, 8, 14, 10, 0)
        );

        when(habitService.getHabitById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/habits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Чтение"))
                .andExpect(jsonPath("$.frequency").value("DAILY"));

        verify(habitService).getHabitById(1L);
    }


    @Test
    void shouldUpdateHabit() throws Exception {

        HabitUpdateRequest request = new HabitUpdateRequest();

        request.setName("Чтение книг");
        request.setDescription("30 минут в день");
        request.setFrequency(Frequency.DAILY);

        HabitResponse response = new HabitResponse(
                1L,
                "Чтение книг",
                "30 минут в день",
                Frequency.DAILY,
                LocalDateTime.of(2026, 8, 14, 10, 0)
        );

        when(habitService.updateHabit(eq(1L), any(HabitUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/habits/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Чтение книг"))
                .andExpect(jsonPath("$.description").value("30 минут в день"))
                .andExpect(jsonPath("$.frequency").value("DAILY"));

        verify(habitService)
                .updateHabit(
                        eq(1L),
                        any(HabitUpdateRequest.class)
                );
    }


    @Test
    void shouldDeleteHabit() throws Exception {

        doNothing()
                .when(habitService)
                .deleteHabit(1L);

        mockMvc.perform(delete("/api/habits/1"))
                .andExpect(status().isNoContent());

        verify(habitService).deleteHabit(1L);
    }

    @Test
    void shouldRejectHabitWithBlankName() throws Exception {

        HabitRequest request = new HabitRequest();

        request.setName("");
        request.setDescription("Читать каждый день");
        request.setFrequency(Frequency.DAILY);

        mockMvc.perform(
                        post("/api/habits")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(habitService, never())
                .createHabit(any(HabitRequest.class));
    }

    @Test
    void shouldRejectHabitWithTooLongName() throws Exception {

        String longName = "a".repeat(101);

        HabitRequest request = new HabitRequest();

        request.setName(longName);
        request.setDescription("Описание привычки");
        request.setFrequency(Frequency.DAILY);

        mockMvc.perform(
                        post("/api/habits")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(habitService, never())
                .createHabit(any(HabitRequest.class));
    }

    @Test
    void shouldRejectHabitWithoutFrequency() throws Exception {

        HabitRequest request = new HabitRequest();

        request.setName("Чтение");
        request.setDescription("Читать каждый день");
        request.setFrequency(null);

        mockMvc.perform(
                        post("/api/habits")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(habitService, never())
                .createHabit(any(HabitRequest.class));
    }

    @Test
    void shouldRejectHabitWithTooLongDescription() throws Exception {

        String longDescription = "a".repeat(501);

        HabitRequest request = new HabitRequest();

        request.setName("Чтение");
        request.setDescription(longDescription);
        request.setFrequency(Frequency.DAILY);

        mockMvc.perform(
                        post("/api/habits")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(habitService, never())
                .createHabit(any(HabitRequest.class));
    }

    @Test
    void shouldRejectUpdateWithBlankName() throws Exception {

        HabitUpdateRequest request = new HabitUpdateRequest();

        request.setName("");
        request.setDescription("Описание привычки");
        request.setFrequency(Frequency.DAILY);

        mockMvc.perform(
                        put("/api/habits/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(habitService, never())
                .updateHabit(
                        eq(1L),
                        any(HabitUpdateRequest.class)
                );
    }

    @Test
    void shouldRejectUpdateWithoutFrequency() throws Exception {

        HabitUpdateRequest request = new HabitUpdateRequest();

        request.setName("Чтение");
        request.setDescription("30 минут в день");
        request.setFrequency(null);

        mockMvc.perform(
                        put("/api/habits/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(habitService, never())
                .updateHabit(
                        eq(1L),
                        any(HabitUpdateRequest.class)
                );
    }

    @Test
    void shouldRejectFrequencyChangeAfterHabitCompletion() throws Exception {

        HabitUpdateRequest request = new HabitUpdateRequest();

        request.setName("Чтение");
        request.setDescription("Читать каждый день");
        request.setFrequency(Frequency.WEEKLY);

        when(habitService.updateHabit(
                eq(1L),
                any(HabitUpdateRequest.class)
        )).thenThrow(new HabitFrequencyChangeException());

        mockMvc.perform(put("/api/habits/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Cannot change habit frequency after it has been completed"));

        verify(habitService).updateHabit(
                eq(1L),
                any(HabitUpdateRequest.class)
        );
    }
}