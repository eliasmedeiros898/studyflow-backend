package com.studyflow.studies;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface SubjectRepository extends JpaRepository<SubjectEntity, UUID> {
    List<SubjectEntity> findByUserIdAndArchivedFalseAndNameContainingIgnoreCaseOrderByName(UUID userId, String query);
    List<SubjectEntity> findByUserIdAndNameContainingIgnoreCaseOrderByName(UUID userId, String query);
    Optional<SubjectEntity> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(UUID userId, String name, UUID id);
    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);
}
