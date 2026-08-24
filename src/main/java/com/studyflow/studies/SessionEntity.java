package com.studyflow.studies;

import com.studyflow.studies.StudyModels.SessionType;
import com.studyflow.users.UserAccount;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "study_sessions")
public class SessionEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;
    @Column(nullable = false, length = 120) private String topic;
    @Column(name = "duration_minutes", nullable = false) private int durationMinutes;
    @Column(name = "studied_on", nullable = false) private LocalDate studiedOn;
    @Column(nullable = false) private int questions;
    @Column(name = "correct_answers", nullable = false) private int correctAnswers;
    @Enumerated(EnumType.STRING) @Column(name = "session_type", nullable = false, length = 30) private SessionType type;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;

    protected SessionEntity() {}
    public SessionEntity(UserAccount user, SubjectEntity subject, String topic, int durationMinutes,
                         LocalDate studiedOn, int questions, int correctAnswers, SessionType type) {
        this.id = UUID.randomUUID(); this.user = user; this.subject = subject; this.topic = topic;
        this.durationMinutes = durationMinutes; this.studiedOn = studiedOn; this.questions = questions;
        this.correctAnswers = correctAnswers; this.type = type; this.createdAt = OffsetDateTime.now();
    }
    public void update(SubjectEntity subject, String topic, int durationMinutes, LocalDate studiedOn,
                       int questions, int correctAnswers, SessionType type) {
        this.subject = subject; this.topic = topic; this.durationMinutes = durationMinutes;
        this.studiedOn = studiedOn; this.questions = questions; this.correctAnswers = correctAnswers;
        this.type = type;
    }
    public UUID getId() { return id; }
    public SubjectEntity getSubject() { return subject; }
    public String getTopic() { return topic; }
    public int getDurationMinutes() { return durationMinutes; }
    public LocalDate getStudiedOn() { return studiedOn; }
    public int getQuestions() { return questions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public SessionType getType() { return type; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
