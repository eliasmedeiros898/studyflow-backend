package com.studyflow.goals;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.UUID;

public final class GoalModels {
    private GoalModels() {}

    public record UpdateGoalRequest(
            @Min(1) @Max(10080) int targetMinutes,
            @Min(1) @Max(100000) int targetQuestions,
            @Min(1) @Max(100) int targetAccuracy
    ) {}

    public record GoalView(UUID id, LocalDate periodStart, LocalDate periodEnd,
                           int targetMinutes, int targetQuestions, int targetAccuracy) {}
}
