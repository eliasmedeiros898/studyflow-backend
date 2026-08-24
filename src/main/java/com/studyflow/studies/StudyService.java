package com.studyflow.studies;

import com.studyflow.studies.StudyModels.*;
import com.studyflow.goals.GoalService;
import com.studyflow.users.UserAccountRepository;
import com.studyflow.users.PreferenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StudyService {
    private final UserAccountRepository users;
    private final SubjectRepository subjects;
    private final TaskRepository tasks;
    private final SessionRepository sessions;
    private final GoalService goals;
    private final PreferenceService preferences;

    public StudyService(UserAccountRepository users, SubjectRepository subjects,
                        TaskRepository tasks, SessionRepository sessions, GoalService goals,
                        PreferenceService preferences) {
        this.users = users; this.subjects = subjects; this.tasks = tasks; this.sessions = sessions;
        this.goals = goals; this.preferences = preferences;
    }

    public List<Subject> listSubjects(UUID userId, String query) {
        return listSubjects(userId, query, false);
    }

    public List<Subject> listSubjects(UUID userId, String query, boolean includeArchived) {
        String normalizedQuery = query == null ? "" : query.trim();
        var found = includeArchived
                ? subjects.findByUserIdAndNameContainingIgnoreCaseOrderByName(userId, normalizedQuery)
                : subjects.findByUserIdAndArchivedFalseAndNameContainingIgnoreCaseOrderByName(userId, normalizedQuery);
        return found
                .stream().map(this::toSubject).toList();
    }

    public List<SubjectMetrics> subjectMetrics(UUID userId) {
        return subjects.findByUserIdAndArchivedFalseAndNameContainingIgnoreCaseOrderByName(userId, "").stream()
                .map(subject -> metrics(subject.getId(),
                        sessions.findByUserIdAndSubjectIdOrderByStudiedOnDescCreatedAtDesc(userId, subject.getId())))
                .toList();
    }

    public SubjectDetails subjectDetails(UUID userId, UUID subjectId) {
        var subject = requireSubject(userId, subjectId);
        var related = sessions.findByUserIdAndSubjectIdOrderByStudiedOnDescCreatedAtDesc(userId, subjectId);
        var grouped = new java.util.LinkedHashMap<String, List<SessionEntity>>();
        related.forEach(session -> grouped.computeIfAbsent(session.getTopic().trim().toLowerCase(), ignored -> new ArrayList<>()).add(session));
        var topics = grouped.values().stream().map(items -> {
            int questions = items.stream().mapToInt(SessionEntity::getQuestions).sum();
            int correct = items.stream().mapToInt(SessionEntity::getCorrectAnswers).sum();
            return new TopicSummary(items.get(0).getTopic(), items.size(),
                    items.stream().mapToInt(SessionEntity::getDurationMinutes).sum(), questions, correct,
                    calculateAccuracy(correct, questions), items.get(0).getStudiedOn());
        }).toList();
        return new SubjectDetails(toSubject(subject), metrics(subjectId, related), topics,
                related.stream().limit(10).map(this::toSession).toList());
    }

    @Transactional
    public Subject createSubject(UUID userId, CreateSubject input) {
        if (subjects.existsByUserIdAndNameIgnoreCase(userId, input.name().trim())) {
            throw new IllegalArgumentException("Já existe uma disciplina com este nome.");
        }
        var user = users.getReferenceById(userId);
        return toSubject(subjects.save(new SubjectEntity(user, input.name().trim(), input.color())));
    }

    @Transactional
    public Subject updateSubject(UUID userId, UUID id, UpdateSubject input) {
        var subject = requireSubject(userId, id);
        if (subjects.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, input.name().trim(), id)) {
            throw new IllegalArgumentException("Já existe uma disciplina com este nome.");
        }
        subject.update(input.name().trim(), input.color());
        return toSubject(subject);
    }

    @Transactional
    public Subject archiveSubject(UUID userId, UUID id) {
        var subject = requireSubject(userId, id);
        subject.archive();
        return toSubject(subject);
    }

    @Transactional
    public Subject restoreSubject(UUID userId, UUID id) {
        var subject = requireSubject(userId, id);
        subject.restore();
        return toSubject(subject);
    }

    public List<StudyTask> listTasks(UUID userId, LocalDate date) {
        List<TaskEntity> found = date == null
                ? tasks.findByUserIdOrderByPlannedDateAscTitleAsc(userId)
                : tasks.findByUserIdAndPlannedDateOrderByTitle(userId, date);
        return found.stream().map(this::toTask).toList();
    }

    public List<StudyTask> listAutomaticReviews(UUID userId) {
        return tasks.findByUserIdAndOriginOrderByPlannedDateAscTitleAsc(userId, TaskOrigin.AUTOMATIC_REVIEW)
                .stream().map(this::toTask).toList();
    }

    public List<TopicProgress> listReviewProgress(UUID userId) {
        Map<String, TaskEntity> pending = new LinkedHashMap<>();
        tasks.findByUserIdAndOriginAndCompletedFalse(userId, TaskOrigin.AUTOMATIC_REVIEW)
                .forEach(task -> pending.put(progressKey(task.getSubject().getId(), task.getReviewTopicKey()), task));

        Map<String, List<SessionEntity>> grouped = new LinkedHashMap<>();
        sessions.findByUserIdOrderByStudiedOnDescCreatedAtDesc(userId).stream()
                .filter(session -> session.getQuestions() > 0)
                .forEach(session -> grouped.computeIfAbsent(
                        progressKey(session.getSubject().getId(), topicKey(session.getTopic())), ignored -> new ArrayList<>())
                        .add(session));

        return grouped.entrySet().stream().map(entry -> {
            List<SessionEntity> items = entry.getValue();
            SessionEntity latest = items.get(0);
            int questions = items.stream().mapToInt(SessionEntity::getQuestions).sum();
            int correct = items.stream().mapToInt(SessionEntity::getCorrectAnswers).sum();
            int accuracy = calculateAccuracy(correct, questions);
            int latestAccuracy = calculateAccuracy(latest.getCorrectAnswers(), latest.getQuestions());
            Integer trend = items.size() < 2 ? null : latestAccuracy
                    - calculateAccuracy(items.get(1).getCorrectAnswers(), items.get(1).getQuestions());
            MasteryStatus status = masteryStatus(items.size(), accuracy, latestAccuracy);
            TaskEntity next = pending.get(entry.getKey());
            List<ReviewAttempt> history = items.stream()
                    .map(item -> new ReviewAttempt(item.getId(), item.getStudiedOn(), item.getQuestions(),
                            item.getCorrectAnswers(), calculateAccuracy(item.getCorrectAnswers(), item.getQuestions()),
                            item.getType()))
                    .toList();
            return new TopicProgress(latest.getSubject().getId(), latest.getSubject().getName(),
                    latest.getSubject().getColor(), latest.getTopic(), items.size(), questions, correct, accuracy,
                    latestAccuracy, trend, status, latest.getStudiedOn(), next == null ? null : next.getId(),
                    next == null ? null : next.getPlannedDate(), history);
        }).sorted(Comparator.comparing(TopicProgress::lastStudiedOn).reversed()).toList();
    }

    public List<StudySession> listSessions(UUID userId, UUID subjectId, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }
        return sessions.findByUserIdOrderByStudiedOnDescCreatedAtDesc(userId).stream()
                .filter(session -> subjectId == null || session.getSubject().getId().equals(subjectId))
                .filter(session -> from == null || !session.getStudiedOn().isBefore(from))
                .filter(session -> to == null || !session.getStudiedOn().isAfter(to))
                .map(this::toSession).toList();
    }

    @Transactional
    public StudyTask createTask(UUID userId, CreateTask input) {
        var user = users.getReferenceById(userId);
        var subject = requireSubject(userId, input.subjectId());
        return toTask(tasks.save(new TaskEntity(user, subject, input.title().trim(), input.date(), input.type())));
    }

    @Transactional
    public StudyTask toggleTask(UUID userId, UUID id) {
        var task = tasks.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Tarefa não encontrada."));
        if (task.isCompleted() && task.getOrigin() == TaskOrigin.AUTOMATIC_REVIEW
                && task.getReviewTopicKey() != null
                && tasks.findOtherPendingAutomaticReview(userId, task.getSubject().getId(),
                        task.getReviewTopicKey(), task.getId()).isPresent()) {
            throw new IllegalArgumentException("Já existe uma revisão pendente para este assunto.");
        }
        task.toggle();
        return toTask(task);
    }

    @Transactional
    public CompleteReviewResult completeReview(UUID userId, UUID id, CompleteReviewRequest input) {
        var task = tasks.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Revisão não encontrada."));
        if (task.getOrigin() != TaskOrigin.AUTOMATIC_REVIEW) {
            throw new IllegalArgumentException("Esta tarefa não é uma revisão automática.");
        }
        if (task.isCompleted()) throw new IllegalArgumentException("Esta revisão já foi concluída.");
        LocalDate nextDate = input == null ? null : input.nextReviewDate();
        if (nextDate != null && !nextDate.isAfter(currentDate(userId))) {
            throw new IllegalArgumentException("A próxima revisão precisa ser agendada para uma data futura.");
        }
        task.complete();
        tasks.flush();
        StudyTask next = null;
        if (nextDate != null) {
            TaskEntity followUp = tasks.save(new TaskEntity(users.getReferenceById(userId), task.getSubject(),
                    task.getTitle(), nextDate, TaskType.REVIEW, TaskOrigin.AUTOMATIC_REVIEW,
                    task.getSourceSession(), task.getReviewTopicKey()));
            next = toTask(followUp);
        }
        return new CompleteReviewResult(toTask(task), next);
    }

    @Transactional
    public StudyTask updateTask(UUID userId, UUID id, UpdateTask input) {
        var task = tasks.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Tarefa não encontrada."));
        task.update(requireSubject(userId, input.subjectId()), input.title().trim(), input.date(), input.type());
        return toTask(task);
    }

    @Transactional
    public void deleteTask(UUID userId, UUID id) {
        var task = tasks.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Tarefa não encontrada."));
        tasks.delete(task);
    }

    @Transactional
    public StudySession createSession(UUID userId, CreateSession input) {
        validateAnswers(input.questions(), input.correctAnswers());
        var user = users.getReferenceById(userId);
        var subject = requireSubject(userId, input.subjectId());
        LocalDate date = input.date() == null ? currentDate(userId) : input.date();
        var session = sessions.save(new SessionEntity(user, subject, input.topic().trim(),
                input.durationMinutes(), date, input.questions(), input.correctAnswers(), input.type()));
        ReviewSchedule review = scheduleAutomaticReview(user, subject, session);
        return toSession(session, review);
    }

    @Transactional
    public StudySession updateSession(UUID userId, UUID id, UpdateSession input) {
        validateAnswers(input.questions(), input.correctAnswers());
        var session = sessions.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Sessão não encontrada."));
        var subject = requireSubject(userId, input.subjectId());
        session.update(subject, input.topic().trim(), input.durationMinutes(), input.date(),
                input.questions(), input.correctAnswers(), input.type());
        ReviewSchedule review = reconcilePendingAutomaticReview(userId, session);
        return toSession(session, review);
    }

    @Transactional
    public void deleteSession(UUID userId, UUID id) {
        var session = sessions.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Sessão não encontrada."));
        tasks.findBySourceSessionIdAndOrigin(id, TaskOrigin.AUTOMATIC_REVIEW)
                .filter(task -> !task.isCompleted()).ifPresent(tasks::delete);
        sessions.delete(session);
    }

    public Dashboard dashboard(UUID userId) {
        LocalDate today = currentDate(userId);
        LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<SessionEntity> weekly = sessions.findByUserIdAndStudiedOnBetween(userId, start, today);
        int minutes = weekly.stream().mapToInt(SessionEntity::getDurationMinutes).sum();
        int questions = weekly.stream().mapToInt(SessionEntity::getQuestions).sum();
        int correct = weekly.stream().mapToInt(SessionEntity::getCorrectAnswers).sum();
        List<DayActivity> activity = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            LocalDate date = start.plusDays(index);
            int dailyMinutes = weekly.stream().filter(session -> session.getStudiedOn().equals(date))
                    .mapToInt(SessionEntity::getDurationMinutes).sum();
            activity.add(new DayActivity(date, dailyMinutes));
        }
        int currentStreak = calculateCurrentStreak(userId, today);
        int bestStreak = calculateBestStreak(userId);
        var goal = goals.current(userId);
        return new Dashboard(minutes, questions, correct, calculateAccuracy(correct, questions), currentStreak,
                bestStreak, goal.targetMinutes(), goal.targetQuestions(), goal.targetAccuracy(), activity, listTasks(userId, today));
    }

    public static int calculateAccuracy(int correct, int questions) {
        return questions == 0 ? 0 : (int) Math.round(correct * 100.0 / questions);
    }

    public static int reviewIntervalDays(int correct, int questions) {
        if (questions == 0) return 0;
        int accuracy = calculateAccuracy(correct, questions);
        if (accuracy < 50) return 1;
        if (accuracy < 70) return 3;
        if (accuracy < 85) return 7;
        return 15;
    }

    private ReviewSchedule scheduleAutomaticReview(com.studyflow.users.UserAccount user, SubjectEntity subject,
                                                    SessionEntity session) {
        int interval = reviewIntervalDays(user.getId(), session.getCorrectAnswers(), session.getQuestions());
        if (interval == 0) return null;
        String title = "Revisar: " + session.getTopic();
        if (title.length() > 120) title = title.substring(0, 120);
        LocalDate reviewDate = session.getStudiedOn().plusDays(interval);
        LocalDate earliestFutureDate = LocalDate.now(ZoneId.of(user.getTimezone())).plusDays(1);
        if (reviewDate.isBefore(earliestFutureDate)) reviewDate = earliestFutureDate;
        String key = topicKey(session.getTopic());
        TaskEntity task = tasks.findFirstByUserIdAndSubjectIdAndReviewTopicKeyAndCompletedFalse(
                user.getId(), subject.getId(), key).orElse(null);
        if (task == null) {
            task = tasks.save(new TaskEntity(user, subject, title, reviewDate, TaskType.REVIEW,
                    TaskOrigin.AUTOMATIC_REVIEW, session, key));
        } else {
            task.rescheduleAutomaticReview(subject, title, reviewDate, session, key);
        }
        return new ReviewSchedule(task.getId(), reviewDate, interval,
                calculateAccuracy(session.getCorrectAnswers(), session.getQuestions()));
    }

    private ReviewSchedule reconcilePendingAutomaticReview(UUID userId, SessionEntity session) {
        var existing = tasks.findBySourceSessionIdAndOrigin(session.getId(), TaskOrigin.AUTOMATIC_REVIEW);
        if (existing.isPresent() && existing.get().isCompleted()) return null;
        int interval = reviewIntervalDays(userId, session.getCorrectAnswers(), session.getQuestions());
        if (interval == 0) {
            existing.ifPresent(tasks::delete);
            return null;
        }
        var user = users.findById(userId).orElseThrow(() -> new NoSuchElementException("Conta não encontrada."));
        LocalDate date = session.getStudiedOn().plusDays(interval);
        LocalDate earliest = LocalDate.now(ZoneId.of(user.getTimezone())).plusDays(1);
        if (date.isBefore(earliest)) date = earliest;
        String title = automaticReviewTitle(session.getTopic());
        String key = topicKey(session.getTopic());
        TaskEntity matching = tasks.findFirstByUserIdAndSubjectIdAndReviewTopicKeyAndCompletedFalse(
                userId, session.getSubject().getId(), key).orElse(null);
        TaskEntity task;
        if (matching != null && (existing.isEmpty() || !matching.getId().equals(existing.get().getId()))) {
            existing.ifPresent(tasks::delete);
            tasks.flush();
            task = matching;
        } else if (existing.isPresent()) {
            task = existing.get();
        } else {
            task = tasks.save(new TaskEntity(user, session.getSubject(), title, date, TaskType.REVIEW,
                    TaskOrigin.AUTOMATIC_REVIEW, session, key));
        }
        task.rescheduleAutomaticReview(session.getSubject(), title, date, session, key);
        return new ReviewSchedule(task.getId(), date, interval,
                calculateAccuracy(session.getCorrectAnswers(), session.getQuestions()));
    }

    private String automaticReviewTitle(String topic) {
        String title = "Revisar: " + topic;
        return title.length() > 120 ? title.substring(0, 120) : title;
    }

    private int reviewIntervalDays(UUID userId, int correct, int questions) {
        if (questions == 0) return 0;
        int accuracy = calculateAccuracy(correct, questions);
        var value = preferences.get(userId);
        if (accuracy < 50) return value.reviewDifficultyDays();
        if (accuracy < 70) return value.reviewDevelopingDays();
        if (accuracy < 85) return value.reviewProficientDays();
        return value.reviewMasteredDays();
    }

    private MasteryStatus masteryStatus(int attempts, int accuracy, int latestAccuracy) {
        if (latestAccuracy < 50 || accuracy < 60) return MasteryStatus.DIFFICULTY;
        if (attempts >= 2 && latestAccuracy >= 85 && accuracy >= 85) return MasteryStatus.MASTERED;
        return MasteryStatus.PROGRESS;
    }

    private String topicKey(String topic) {
        return topic.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private String progressKey(UUID subjectId, String topicKey) {
        return subjectId + "|" + (topicKey == null ? "" : topicKey);
    }

    private void validateAnswers(int questions, int correctAnswers) {
        if (correctAnswers > questions) {
            throw new IllegalArgumentException("Acertos não podem ser maiores que o total de questões.");
        }
    }

    private int calculateCurrentStreak(UUID userId, LocalDate today) {
        int streak = 0;
        LocalDate cursor = hasSession(userId, today) ? today : today.minusDays(1);
        while (hasSession(userId, cursor)) { streak++; cursor = cursor.minusDays(1); }
        return streak;
    }

    private int calculateBestStreak(UUID userId) {
        var dates = sessions.findByUserIdOrderByStudiedOnAsc(userId).stream()
                .map(SessionEntity::getStudiedOn).distinct().toList();
        return calculateBestStreak(dates);
    }

    static int calculateBestStreak(List<LocalDate> dates) {
        int best = 0, streak = 0;
        LocalDate previous = null;
        for (LocalDate date : dates) {
            streak = previous != null && date.equals(previous.plusDays(1)) ? streak + 1 : 1;
            best = Math.max(best, streak); previous = date;
        }
        return best;
    }

    private boolean hasSession(UUID userId, LocalDate date) {
        return sessions.existsByUserIdAndStudiedOnAndDurationMinutesGreaterThanEqual(userId, date, 1);
    }

    private LocalDate currentDate(UUID userId) {
        var user = users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Conta não encontrada."));
        return LocalDate.now(ZoneId.of(user.getTimezone()));
    }

    private SubjectEntity requireSubject(UUID userId, UUID id) {
        return subjects.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Disciplina não encontrada."));
    }

    private Subject toSubject(SubjectEntity entity) {
        return new Subject(entity.getId(), entity.getName(), entity.getColor(), entity.isArchived());
    }
    private SubjectMetrics metrics(UUID subjectId, List<SessionEntity> related) {
        int questions = related.stream().mapToInt(SessionEntity::getQuestions).sum();
        int correct = related.stream().mapToInt(SessionEntity::getCorrectAnswers).sum();
        return new SubjectMetrics(subjectId, related.stream().mapToInt(SessionEntity::getDurationMinutes).sum(),
                related.size(), questions, correct, calculateAccuracy(correct, questions),
                related.isEmpty() ? null : related.get(0).getStudiedOn());
    }
    private StudyTask toTask(TaskEntity entity) {
        return new StudyTask(entity.getId(), entity.getSubject().getId(), entity.getSubject().getName(),
                entity.getSubject().getColor(), entity.getTitle(), entity.getPlannedDate(),
                entity.getType(), entity.isCompleted(), entity.getCompletedAt(), entity.getOrigin(),
                entity.getSourceSession() == null ? null : entity.getSourceSession().getId());
    }
    private StudySession toSession(SessionEntity entity) {
        return toSession(entity, null);
    }
    private StudySession toSession(SessionEntity entity, ReviewSchedule scheduledReview) {
        return new StudySession(entity.getId(), entity.getSubject().getId(), entity.getSubject().getName(),
                entity.getSubject().getColor(), entity.getTopic(),
                entity.getDurationMinutes(), entity.getStudiedOn(), entity.getQuestions(),
                entity.getCorrectAnswers(), entity.getType(),
                calculateAccuracy(entity.getCorrectAnswers(), entity.getQuestions()), entity.getCreatedAt(), scheduledReview);
    }
}
