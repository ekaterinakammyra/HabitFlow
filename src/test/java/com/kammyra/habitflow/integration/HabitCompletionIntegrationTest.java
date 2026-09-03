package com.kammyra.habitflow.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class HabitCompletionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private Long createHabit() throws Exception {

        String requestBody = """
                {
                    "name": "Чтение",
                    "description": "Прочитать 30 страниц",
                    "frequency": "DAILY"
                }
                """;

        String response = mockMvc.perform(
                        post("/api/habits")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response)
                .get("id")
                .asLong();
    }

    @Test
    void shouldCompleteHabit() throws Exception {

        Long habitId = createHabit();

        mockMvc.perform(
                        post("/api/habits/" + habitId + "/completions")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.habitId").value(habitId))
                .andExpect(jsonPath("$.completionDate").exists());
    }

    @Test
    void shouldCompleteHabitWithCustomDate() throws Exception {

        Long habitId = createHabit();

        LocalDate today = LocalDate.now();

        String requestBody = """
            {
                "completionDate": "%s"
            }
            """.formatted(today);

        mockMvc.perform(
                        post("/api/habits/" + habitId + "/completions")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.habitId").value(habitId))
                .andExpect(jsonPath("$.completionDate").value(today.toString()));
    }

    @Test
    void shouldNotCompleteHabitTwiceForSameDate() throws Exception {

        Long habitId = createHabit();

        LocalDate today = LocalDate.now();

        String requestBody = """
            {
                "completionDate": "%s"
            }
            """.formatted(today);

        mockMvc.perform(
                        post("/api/habits/" + habitId + "/completions")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/habits/" + habitId + "/completions")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenCompletingNonExistingHabit() throws Exception {

        String requestBody = """
                {
                    "completionDate": "2026-08-20"
                }
                """;

        mockMvc.perform(
                        post("/api/habits/999999/completions")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCompleteHabitWithCurrentDate() throws Exception {
        Long habitId = createHabit();

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.habitId").value(habitId))
                .andExpect(jsonPath("$.completionDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    void shouldNotCompleteHabitTwiceOnSameDate() throws Exception {
        Long habitId = createHabit();

        LocalDate today = LocalDate.now();
        String request = """
            {
                "completionDate": "%s"
            }
            """.formatted(today);

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundWhenHabitDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/habits/{habitId}/completions", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "completionDate": "2026-08-20"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectNullCompletionDate() throws Exception {
        Long habitId = createHabit();

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "completionDate": null
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completionDate").value(LocalDate.now().toString()));
    }

    @Test
    void shouldRejectInvalidCompletionDate() throws Exception {
        Long habitId = createHabit();

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "completionDate": "not-a-date"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnCorrectHabitId() throws Exception {

        Long habitId = createHabit();

        LocalDate today = LocalDate.now();

        mockMvc.perform(
                        post("/api/habits/{habitId}/completions", habitId)
                                .contentType("application/json")
                                .content("""
                                {
                                    "completionDate": "%s"
                                }
                                """.formatted(today)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.habitId").value(habitId));
    }

    @Test
    void shouldRejectFutureCompletionDate() throws Exception {
        Long habitId = createHabit();

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        mockMvc.perform(post("/api/habits/{habitId}/completions", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "completionDate": "%s"
                            }
                            """.formatted(tomorrow)))
                .andExpect(status().isBadRequest());
    }
}