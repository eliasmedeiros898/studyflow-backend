package com.studyflow.users;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public final class PreferenceModels {
    private PreferenceModels() {}

    public record PreferencesView(int focusMinutes, int shortBreakMinutes, int longBreakMinutes,
                                  int cycles, boolean soundEnabled, boolean browserNotifications,
                                  int reviewDifficultyDays, int reviewDevelopingDays,
                                  int reviewProficientDays, int reviewMasteredDays) {}

    public record UpdatePreferencesRequest(
            @Min(1) @Max(180) int focusMinutes,
            @Min(1) @Max(60) int shortBreakMinutes,
            @Min(1) @Max(120) int longBreakMinutes,
            @Min(1) @Max(8) int cycles,
            boolean soundEnabled,
            boolean browserNotifications,
            @Min(1) @Max(90) int reviewDifficultyDays,
            @Min(1) @Max(90) int reviewDevelopingDays,
            @Min(1) @Max(90) int reviewProficientDays,
            @Min(1) @Max(180) int reviewMasteredDays
    ) {}
}
