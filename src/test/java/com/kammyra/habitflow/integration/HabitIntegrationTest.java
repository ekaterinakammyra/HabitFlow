package com.kammyra.habitflow.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class HabitIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE habit_completion, habit RESTART IDENTITY CASCADE");
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldCreateHabit() throws Exception {

        String requestBody = """
                {
                    "name": "Чтение",
                    "description": "Прочитать 30 страниц",
                    "frequency": "DAILY"
                }
                """;

        mockMvc.perform(
                post("/api/habits")
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(
                status().isCreated()
        );
    }

    @Test
    void shouldGetHabitById() throws Exception {

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

        Long id = objectMapper.readTree(response)
                .get("id")
                .asLong();

        mockMvc.perform(
                        get("/api/habits/" + id)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Чтение"))
                .andExpect(jsonPath("$.description").value("Прочитать 30 страниц"))
                .andExpect(jsonPath("$.frequency").value("DAILY"));
    }

    @Test
    void shouldGetAllHabits() throws Exception {

        String firstHabit = """
            {
                "name": "Чтение",
                "description": "Прочитать 30 страниц",
                "frequency": "DAILY"
            }
            """;

        String secondHabit = """
            {
                "name": "Тренировка",
                "description": "Тренировка 3 раза",
                "frequency": "WEEKLY"
            }
            """;

        mockMvc.perform(
                        post("/api/habits")
                                .contentType("application/json")
                                .content(firstHabit)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/habits")
                                .contentType("application/json")
                                .content(secondHabit)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/habits")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Чтение"))
                .andExpect(jsonPath("$[1].name").value("Тренировка"));
    }

    @Test
    void shouldUpdateHabit() throws Exception {

        String createRequest = """
            {
                "name": "Чтение",
                "description": "Прочитать 30 страниц",
                "frequency": "DAILY"
            }
            """;

        String response = mockMvc.perform(
                        post("/api/habits")
                                .contentType("application/json")
                                .content(createRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response)
                .get("id")
                .asLong();

        String updateRequest = """
            {
                "name": "Чтение двух книг",
                "description": "Прочитать 40 страниц",
                "frequency": "WEEKLY"
            }
            """;

        mockMvc.perform(
                        put("/api/habits/" + id)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Чтение двух книг"))
                .andExpect(jsonPath("$.description").value("Прочитать 40 страниц"))
                .andExpect(jsonPath("$.frequency").value("WEEKLY"));
    }

    @Test
    void shouldDeleteHabit() throws Exception {

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

        Long id = objectMapper.readTree(response)
                .get("id")
                .asLong();

        mockMvc.perform(
                        delete("/api/habits/" + id)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/habits/" + id)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenHabitNotFound() throws Exception {

        mockMvc.perform(
                        get("/api/habits/999999")
                )
                .andExpect(status().isNotFound());
    }
}