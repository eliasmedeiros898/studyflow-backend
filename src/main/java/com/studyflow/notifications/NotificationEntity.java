package com.studyflow.notifications;

import com.studyflow.users.UserAccount;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;
    @Enumerated(EnumType.STRING) @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationModels.NotificationType type;
    @Column(nullable = false, length = 120) private String title;
    @Column(nullable = false, length = 300) private String message;
    @Column(name = "action_target", nullable = false, length = 30) private String actionTarget;
    @Column(name = "related_task_id") private UUID relatedTaskId;
    @Column(name = "dedup_key", nullable = false, length = 180) private String dedupKey;
    @Column(name = "read_at") private OffsetDateTime readAt;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;

    protected NotificationEntity() {}
    public NotificationEntity(UserAccount user, NotificationModels.NotificationType type, String title,
                              String message, String actionTarget, UUID relatedTaskId, String dedupKey) {
        this.id = UUID.randomUUID(); this.user = user; this.type = type; this.title = title;
        this.message = message; this.actionTarget = actionTarget; this.relatedTaskId = relatedTaskId;
        this.dedupKey = dedupKey; this.createdAt = OffsetDateTime.now();
    }
    public void markRead() { if (readAt == null) readAt = OffsetDateTime.now(); }
    public UUID getId() { return id; }
    public NotificationModels.NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getActionTarget() { return actionTarget; }
    public UUID getRelatedTaskId() { return relatedTaskId; }
    public OffsetDateTime getReadAt() { return readAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

