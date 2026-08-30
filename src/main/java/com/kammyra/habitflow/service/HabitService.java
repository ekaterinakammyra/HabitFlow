package com.kammyra.habitflow.service;

import com.kammyra.habitflow.dto.HabitRequest;
import com.kammyra.habitflow.dto.HabitResponse;
import com.kammyra.habitflow.dto.HabitUpdateRequest;
import com.kammyra.habitflow.entity.Habit;
import com.kammyra.habitflow.exception.HabitNotFoundException;
import com.kammyra.habitflow.repository.HabitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class HabitService {

    private final HabitRepository habitRepository;
    private final Clock clock;

    public HabitService(HabitRepository habitRepository, Clock clock) {
        this.habitRepository = habitRepository;
        this.clock = clock;
    }

    public HabitResponse createHabit(HabitRequest request) {
        Habit habit = new Habit(
                request.getName(),
                request.getDescription(),
                request.getFrequency(),
                LocalDateTime.now(clock)
        );
        Habit savedHabit = habitRepository.save(habit);
        return toResponse(savedHabit);
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> getAllHabits() {
        return habitRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitResponse getHabitById(Long id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new HabitNotFoundException(id));

        return toResponse(habit);
    }

    public HabitResponse updateHabit(
            Long id,
            HabitUpdateRequest request
    ) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new HabitNotFoundException(id));

        habit.setName(request.getName());
        habit.setDescription(request.getDescription());
        habit.setFrequency(request.getFrequency());

        return toResponse(habitRepository.save(habit));
    }

    public void deleteHabit(Long id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new HabitNotFoundException(id));

        habitRepository.delete(habit);
    }

    private HabitResponse toResponse(Habit habit) {
        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.getFrequency(),
                habit.getCreatedAt()
        );
    }
}