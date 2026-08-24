package com.studyflow.studies;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {
    List<SessionEntity> findByUserIdAndStudiedOnBetween(UUID userId, LocalDate start, LocalDate end);
    List<SessionEntity> findByUserIdOrderByStudiedOnAsc(UUID userId);
    List<SessionEntity> findByUserIdOrderByStudiedOnDescCreatedAtDesc(UUID userId);
    java.util.Optional<SessionEntity> findByIdAndUserId(UUID id, UUID userId);
    List<SessionEntity> findByUserIdAndSubjectIdOrderByStudiedOnDescCreatedAtDesc(UUID userId, UUID subjectId);
    boolean existsByUserIdAndStudiedOnAndDurationMinutesGreaterThanEqual(UUID userId, LocalDate date, int minimumMinutes);
}
