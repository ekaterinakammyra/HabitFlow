package com.kammyra.habitflow.dto;

public class HabitStatisticsResponse {

    private Long habitId;
    private String habitName;
    private long totalCompletions;
    private int currentStreak;
    private int bestStreak;

    public HabitStatisticsResponse(
            Long habitId,
            String habitName,
            long totalCompletions,
            int currentStreak,
            int bestStreak
    ) {
        this.habitId = habitId;
        this.habitName = habitName;
        this.totalCompletions = totalCompletions;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
    }

    public Long getHabitId() {
        return habitId;
    }

    public String getHabitName() {
        return habitName;
    }

    public long getTotalCompletions() {
        return totalCompletions;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }
}