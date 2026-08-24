package com.studyflow.analytics;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AnalyticsModels {
    private AnalyticsModels() {}

    public enum Period { WEEK, MONTH, YEAR }

    public record TimelinePoint(String label, LocalDate date, int minutes, int questions) {}
    public record SubjectPerformance(UUID subjectId, String name, String color, int minutes,
                                     int questions, int correctAnswers, int accuracy, int sharePercent) {}
    public record PerformanceView(Period period, LocalDate periodStart, LocalDate periodEnd,
                                  int minutes, int questions, int correctAnswers, int accuracy,
                                  int previousMinutes, List<TimelinePoint> timeline,
                                  List<SubjectPerformance> subjects) {}
}
