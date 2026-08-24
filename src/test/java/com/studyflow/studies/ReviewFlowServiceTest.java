package com.studyflow.studies;

import com.studyflow.goals.GoalService;
import com.studyflow.studies.StudyModels.CompleteReviewRequest;
import com.studyflow.studies.StudyModels.SessionType;
import com.studyflow.studies.StudyModels.TaskOrigin;
import com.studyflow.studies.StudyModels.TaskType;
import com.studyflow.users.PreferenceService;
import com.studyflow.users.UserAccount;
import com.studyflow.users.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewFlowServiceTest {
    @Mock UserAccountRepository users;
    @Mock SubjectRepository subjects;
    @Mock TaskRepository tasks;
    @Mock SessionRepository sessions;
    @Mock GoalService goals;
    @Mock PreferenceService preferences;
    StudyService service;

    @BeforeEach
    void setup() {
        service = new StudyService(users, subjects, tasks, sessions, goals, preferences);
    }

    @Test
    void completesReviewByPersistingResultAndLinkingTheNextReview() {
        var user = user();
        var subject = new SubjectEntity(user, "Eng. Comp 1", "#F2A055");
        var source = new SessionEntity(user, subject, "Big O complexity", 120,
                LocalDate.of(2026, 8, 21), 20, 12, SessionType.FIRST_CONTACT);
        var review = new TaskEntity(user, subject, "Revisar: Big O complexity",
                LocalDate.of(2026, 8, 24), TaskType.REVIEW, TaskOrigin.AUTOMATIC_REVIEW, source, null);
        var today = LocalDate.now(ZoneId.of(user.getTimezone()));

        when(tasks.findByIdAndUserId(review.getId(), user.getId())).thenReturn(Optional.of(review));
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.getReferenceById(user.getId())).thenReturn(user);
        when(sessions.save(any(SessionEntity.class))).thenAnswer(call -> call.getArgument(0));
        when(tasks.save(any(TaskEntity.class))).thenAnswer(call -> call.getArgument(0));

        var result = service.completeReview(user.getId(), review.getId(),
                new CompleteReviewRequest(today.plusDays(3), today, 30, 10, 8));

        assertThat(result.completedReview().completed()).isTrue();
        assertThat(result.recordedSession()).isNotNull();
        assertThat(result.recordedSession().type()).isEqualTo(SessionType.REVIEW);
        assertThat(result.recordedSession().accuracy()).isEqualTo(80);
        assertThat(result.nextReview()).isNotNull();
        assertThat(result.nextReview().sourceSessionId()).isEqualTo(result.recordedSession().id());
    }

    @Test
    void comparesLatestResultWithThePreviousAttemptAndConsolidatesByQuestionCount() {
        var user = user();
        var subject = new SubjectEntity(user, "Eng. Comp 1", "#F2A055");
        var latest = new SessionEntity(user, subject, "Big O complexity", 30,
                LocalDate.of(2026, 8, 24), 10, 8, SessionType.REVIEW);
        var previous = new SessionEntity(user, subject, "Big O complexity", 120,
                LocalDate.of(2026, 8, 21), 20, 12, SessionType.FIRST_CONTACT);
        var pending = new TaskEntity(user, subject, "Revisar: Big O complexity",
                LocalDate.of(2026, 8, 27), TaskType.REVIEW, TaskOrigin.AUTOMATIC_REVIEW,
                latest, "big o complexity");

        when(tasks.findByUserIdAndOriginAndCompletedFalse(user.getId(), TaskOrigin.AUTOMATIC_REVIEW))
                .thenReturn(List.of(pending));
        when(sessions.findByUserIdOrderByStudiedOnDescCreatedAtDesc(user.getId()))
                .thenReturn(List.of(latest, previous));

        var progress = service.listReviewProgress(user.getId()).get(0);

        assertThat(progress.latestAccuracy()).isEqualTo(80);
        assertThat(progress.trend()).isEqualTo(20);
        assertThat(progress.accuracy()).isEqualTo(67);
        assertThat(progress.answeredSessions()).isEqualTo(2);
        assertThat(progress.pendingReviewTaskId()).isEqualTo(pending.getId());
        assertThat(progress.history()).extracting(item -> item.accuracy()).containsExactly(80, 60);
    }

    @Test
    void exposesAutomaticReviewsInsideSubjectDetails() {
        var user = user();
        var subject = new SubjectEntity(user, "Eng. Comp 1", "#F2A055");
        var session = new SessionEntity(user, subject, "BD1", 20,
                LocalDate.of(2026, 8, 21), 15, 10, SessionType.FIRST_CONTACT);
        var review = new TaskEntity(user, subject, "Revisar: BD1",
                LocalDate.of(2026, 8, 25), TaskType.REVIEW, TaskOrigin.AUTOMATIC_REVIEW,
                session, "bd1");

        when(subjects.findByIdAndUserId(subject.getId(), user.getId())).thenReturn(Optional.of(subject));
        when(sessions.findByUserIdAndSubjectIdOrderByStudiedOnDescCreatedAtDesc(user.getId(), subject.getId()))
                .thenReturn(List.of(session));
        when(tasks.findTop20ByUserIdAndSubjectIdAndOriginOrderByPlannedDateDesc(
                user.getId(), subject.getId(), TaskOrigin.AUTOMATIC_REVIEW)).thenReturn(List.of(review));

        var details = service.subjectDetails(user.getId(), subject.getId());

        assertThat(details.reviews()).hasSize(1);
        assertThat(details.reviews().get(0).title()).isEqualTo("Revisar: BD1");
    }

    @Test
    void preventsCompletingAutomaticReviewThroughGenericTaskToggle() {
        var user = user();
        var subject = new SubjectEntity(user, "Eng. Comp 1", "#F2A055");
        var source = new SessionEntity(user, subject, "BD1", 20,
                LocalDate.of(2026, 8, 21), 15, 10, SessionType.FIRST_CONTACT);
        var review = new TaskEntity(user, subject, "Revisar: BD1",
                LocalDate.of(2026, 8, 25), TaskType.REVIEW, TaskOrigin.AUTOMATIC_REVIEW,
                source, "bd1");
        when(tasks.findByIdAndUserId(review.getId(), user.getId())).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> service.toggleTask(user.getId(), review.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Central de revisões");
    }

    private UserAccount user() {
        return new UserAccount("Elias", "elias@example.com", "hash", "America/Sao_Paulo");
    }
}
