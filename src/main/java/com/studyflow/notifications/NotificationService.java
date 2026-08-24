package com.studyflow.notifications;

import com.studyflow.notifications.NotificationModels.*;
import com.studyflow.studies.StudyModels.TaskOrigin;
import com.studyflow.studies.TaskEntity;
import com.studyflow.studies.TaskRepository;
import com.studyflow.users.UserAccount;
import com.studyflow.users.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notifications;
    private final TaskRepository tasks;
    private final UserAccountRepository users;

    public NotificationService(NotificationRepository notifications, TaskRepository tasks, UserAccountRepository users) {
        this.notifications = notifications; this.tasks = tasks; this.users = users;
    }

    @Transactional
    public NotificationCenter center(UUID userId) {
        UserAccount user = users.findById(userId).orElseThrow(() -> new NoSuchElementException("Conta não encontrada."));
        LocalDate today = LocalDate.now(ZoneId.of(user.getTimezone()));
        tasks.findByUserIdOrderByPlannedDateAscTitleAsc(userId).stream()
                .filter(task -> !task.isCompleted()).forEach(task -> generate(user, task, today));
        var items = notifications.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toModel).toList();
        return new NotificationCenter(items.stream().filter(item -> !item.read()).count(), items);
    }

    @Transactional
    public Notification markRead(UUID userId, UUID id) {
        var notification = notifications.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Notificação não encontrada."));
        notification.markRead();
        return toModel(notification);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notifications.findByUserIdOrderByCreatedAtDesc(userId).forEach(NotificationEntity::markRead);
    }

    private void generate(UserAccount user, TaskEntity task, LocalDate today) {
        NotificationType type = null;
        String title = null, message = null, target = "CALENDAR";
        boolean automaticReview = task.getOrigin() == TaskOrigin.AUTOMATIC_REVIEW;
        if (automaticReview && task.getPlannedDate().isBefore(today)) {
            type = NotificationType.REVIEW_OVERDUE; title = "Revisão atrasada";
            message = task.getSubject().getName() + ": " + task.getTitle().replaceFirst("^Revisar: ", ""); target = "REVIEWS";
        } else if (task.getPlannedDate().equals(today)) {
            type = automaticReview ? NotificationType.REVIEW_TODAY : NotificationType.TASK_TODAY;
            title = automaticReview ? "Revisão para hoje" : "Tarefa para hoje";
            message = task.getSubject().getName() + ": " + task.getTitle().replaceFirst("^Revisar: ", "");
            target = automaticReview ? "REVIEWS" : "CALENDAR";
        } else if (automaticReview && task.getPlannedDate().equals(today.plusDays(1))) {
            type = NotificationType.REVIEW_TOMORROW; title = "Revisão amanhã";
            message = task.getSubject().getName() + ": " + task.getTitle().replaceFirst("^Revisar: ", ""); target = "REVIEWS";
        }
        if (type == null) return;
        String dedupKey = type + ":" + task.getId() + ":" + task.getPlannedDate();
        if (!notifications.existsByUserIdAndDedupKey(user.getId(), dedupKey)) {
            notifications.save(new NotificationEntity(user, type, title, message, target, task.getId(), dedupKey));
        }
    }

    private Notification toModel(NotificationEntity entity) {
        return new Notification(entity.getId(), entity.getType(), entity.getTitle(), entity.getMessage(),
                entity.getActionTarget(), entity.getRelatedTaskId(), entity.getReadAt() != null, entity.getCreatedAt());
    }
}

