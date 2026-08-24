package com.studyflow.studies;

import com.studyflow.studies.StudyModels.TaskType;
import com.studyflow.studies.StudyModels.TaskOrigin;
import com.studyflow.users.UserAccount;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "study_tasks")
public class TaskEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;
    @Column(nullable = false, length = 120) private String title;
    @Column(name = "planned_date", nullable = false) private LocalDate plannedDate;
    @Enumerated(EnumType.STRING) @Column(name = "task_type", nullable = false, length = 30) private TaskType type;
    @Column(nullable = false) private boolean completed;
    @Column(name = "completed_at") private OffsetDateTime completedAt;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TaskOrigin origin;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "source_session_id") private SessionEntity sourceSession;
    @Column(name = "review_topic_key", length = 120) private String reviewTopicKey;

    protected TaskEntity() {}
    public TaskEntity(UserAccount user, SubjectEntity subject, String title, LocalDate plannedDate, TaskType type) {
        this.id = UUID.randomUUID(); this.user = user; this.subject = subject; this.title = title;
        this.plannedDate = plannedDate; this.type = type; this.origin = TaskOrigin.MANUAL; this.createdAt = OffsetDateTime.now();
    }
    public TaskEntity(UserAccount user, SubjectEntity subject, String title, LocalDate plannedDate,
                      TaskType type, TaskOrigin origin, SessionEntity sourceSession, String reviewTopicKey) {
        this(user, subject, title, plannedDate, type); this.origin = origin; this.sourceSession = sourceSession;
        this.reviewTopicKey = reviewTopicKey;
    }
    public void toggle() { completed = !completed; completedAt = completed ? OffsetDateTime.now() : null; }
    public void update(SubjectEntity subject, String title, LocalDate plannedDate, TaskType type) {
        this.subject = subject; this.title = title; this.plannedDate = plannedDate; this.type = type;
    }
    public void rescheduleAutomaticReview(SubjectEntity subject, String title, LocalDate plannedDate,
                                          SessionEntity sourceSession, String reviewTopicKey) {
        this.subject = subject; this.title = title; this.plannedDate = plannedDate;
        this.type = TaskType.REVIEW; this.sourceSession = sourceSession; this.reviewTopicKey = reviewTopicKey;
    }
    public void complete() { this.completed = true; this.completedAt = OffsetDateTime.now(); }
    public UUID getId() { return id; }
    public SubjectEntity getSubject() { return subject; }
    public String getTitle() { return title; }
    public LocalDate getPlannedDate() { return plannedDate; }
    public TaskType getType() { return type; }
    public boolean isCompleted() { return completed; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public TaskOrigin getOrigin() { return origin; }
    public SessionEntity getSourceSession() { return sourceSession; }
    public String getReviewTopicKey() { return reviewTopicKey; }
}
