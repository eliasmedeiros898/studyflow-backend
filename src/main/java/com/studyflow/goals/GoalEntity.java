package com.studyflow.goals;

import com.studyflow.users.UserAccount;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "study_goals")
public class GoalEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;
    @Column(name = "period_start", nullable = false) private LocalDate periodStart;
    @Column(name = "period_end", nullable = false) private LocalDate periodEnd;
    @Column(name = "target_minutes", nullable = false) private int targetMinutes;
    @Column(name = "target_questions", nullable = false) private int targetQuestions;
    @Column(name = "target_accuracy", nullable = false) private int targetAccuracy;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    protected GoalEntity() {}

    public GoalEntity(UserAccount user, LocalDate periodStart, LocalDate periodEnd,
                      int targetMinutes, int targetQuestions, int targetAccuracy) {
        this.id = UUID.randomUUID(); this.user = user; this.periodStart = periodStart; this.periodEnd = periodEnd;
        this.targetMinutes = targetMinutes; this.targetQuestions = targetQuestions; this.targetAccuracy = targetAccuracy;
        this.createdAt = OffsetDateTime.now(); this.updatedAt = this.createdAt;
    }

    public void update(int targetMinutes, int targetQuestions, int targetAccuracy) {
        this.targetMinutes = targetMinutes; this.targetQuestions = targetQuestions;
        this.targetAccuracy = targetAccuracy; this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public int getTargetMinutes() { return targetMinutes; }
    public int getTargetQuestions() { return targetQuestions; }
    public int getTargetAccuracy() { return targetAccuracy; }
}
