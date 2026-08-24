package com.studyflow.studies;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByUserIdAndPlannedDateOrderByTitle(UUID userId, LocalDate date);
    List<TaskEntity> findByUserIdOrderByPlannedDateAscTitleAsc(UUID userId);
    Optional<TaskEntity> findByIdAndUserId(UUID id, UUID userId);
    List<TaskEntity> findByUserIdAndOriginOrderByPlannedDateAscTitleAsc(UUID userId, StudyModels.TaskOrigin origin);
    Optional<TaskEntity> findBySourceSessionIdAndOrigin(UUID sourceSessionId, StudyModels.TaskOrigin origin);
    List<TaskEntity> findByUserIdAndOriginAndCompletedFalse(UUID userId, StudyModels.TaskOrigin origin);
    Optional<TaskEntity> findFirstByUserIdAndSubjectIdAndReviewTopicKeyAndCompletedFalse(
            UUID userId, UUID subjectId, String reviewTopicKey);

    @Query("select t from TaskEntity t where t.user.id = :userId and t.subject.id = :subjectId " +
            "and t.reviewTopicKey = :topicKey and t.completed = false and t.origin = 'AUTOMATIC_REVIEW' and t.id <> :excludedId")
    Optional<TaskEntity> findOtherPendingAutomaticReview(@Param("userId") UUID userId,
            @Param("subjectId") UUID subjectId, @Param("topicKey") String topicKey,
            @Param("excludedId") UUID excludedId);
}
