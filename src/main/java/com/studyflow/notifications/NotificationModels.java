package com.studyflow.notifications;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class NotificationModels {
    private NotificationModels() {}
    public enum NotificationType { TASK_TODAY, REVIEW_TODAY, REVIEW_TOMORROW, REVIEW_OVERDUE }
    public record Notification(UUID id, NotificationType type, String title, String message,
                               String actionTarget, UUID relatedTaskId, boolean read, OffsetDateTime createdAt) {}
    public record NotificationCenter(long unreadCount, List<Notification> notifications) {}
}

