package com.studyflow.studies;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class StudyModels {
    private StudyModels() {}

    public record Subject(UUID id, String name, String color, boolean archived) {}

    public record CreateSubject(
            @NotBlank @Size(max = 80) String name,
            @NotBlank String color
    ) {}

    public record UpdateSubject(
            @NotBlank @Size(max = 80) String name,
            @NotBlank String color
    ) {}

    public enum TaskType { FIRST_CONTACT, REVIEW, EXAM, GENERAL }
    public enum TaskOrigin { MANUAL, AUTOMATIC_REVIEW }

    public record StudyTask(
            UUID id, UUID subjectId, String subjectName, String subjectColor, String title, LocalDate date,
            TaskType type, boolean completed, OffsetDateTime completedAt,
            TaskOrigin origin, UUID sourceSessionId
    ) {}

    public record CreateTask(
            @NotNull UUID subjectId,
            @NotBlank @Size(max = 120) String title,
            @NotNull LocalDate date,
            @NotNull TaskType type
    ) {}

    public record UpdateTask(
            @NotNull UUID subjectId,
            @NotBlank @Size(max = 120) String title,
            @NotNull LocalDate date,
            @NotNull TaskType type
    ) {}

    public enum SessionType { FIRST_CONTACT, REVIEW, MOCK_EXAM, OTHER }

    public record StudySession(
            UUID id, UUID subjectId, String subjectName, String subjectColor, String topic, int durationMinutes,
            LocalDate date, int questions, int correctAnswers, SessionType type,
            int accuracy, OffsetDateTime createdAt, ReviewSchedule scheduledReview
    ) {}

    public record ReviewSchedule(UUID taskId, LocalDate date, int intervalDays, int accuracy) {}

    public enum MasteryStatus { DIFFICULTY, PROGRESS, MASTERED }

    public record ReviewAttempt(
            UUID sessionId, LocalDate date, int questions, int correctAnswers,
            int accuracy, SessionType type
    ) {}

    public record TopicProgress(
            UUID subjectId, String subjectName, String subjectColor, String topic,
            int answeredSessions, int questions, int correctAnswers, int accuracy,
            int latestAccuracy, Integer trend, MasteryStatus status, LocalDate lastStudiedOn,
            UUID pendingReviewTaskId, LocalDate nextReviewDate, List<ReviewAttempt> history
    ) {}

    public record CompleteReviewRequest(
            LocalDate nextReviewDate,
            LocalDate studiedOn,
            @Min(0) @Max(1440) int durationMinutes,
            @Min(0) int questions,
            @Min(0) int correctAnswers
    ) {}

    public record CompleteReviewResult(
            StudyTask completedReview,
            StudyTask nextReview,
            StudySession recordedSession
    ) {}

    public record SubjectMetrics(
            UUID subjectId, int minutes, int sessionCount, int questions, int correctAnswers,
            int accuracy, LocalDate lastStudiedOn
    ) {}

    public record TopicSummary(
            String topic, int sessionCount, int minutes, int questions, int correctAnswers,
            int accuracy, LocalDate lastStudiedOn
    ) {}

    public record SubjectDetails(
            Subject subject, SubjectMetrics metrics, List<TopicSummary> topics,
            List<StudySession> recentSessions, List<StudyTask> reviews
    ) {}

    public record CreateSession(
            @NotNull UUID subjectId,
            @NotBlank @Size(max = 120) String topic,
            @Min(1) @Max(1440) int durationMinutes,
            LocalDate date,
            @Min(0) int questions,
            @Min(0) int correctAnswers,
            @NotNull SessionType type
    ) {}

    public record UpdateSession(
            @NotNull UUID subjectId,
            @NotBlank @Size(max = 120) String topic,
            @Min(1) @Max(1440) int durationMinutes,
            @NotNull LocalDate date,
            @Min(0) int questions,
            @Min(0) int correctAnswers,
            @NotNull SessionType type
    ) {}

    public record DayActivity(LocalDate date, int minutes) {}

    public record Dashboard(
            int minutesStudied,
            int questionsAnswered,
            int correctAnswers,
            int accuracy,
            int currentStreak,
            int bestStreak,
            int weeklyGoalMinutes,
            int weeklyGoalQuestions,
            int targetAccuracy,
            List<DayActivity> activity,
            List<StudyTask> todayTasks
    ) {}
}
