package com.studyflow.goals;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
    Optional<GoalEntity> findByUserIdAndPeriodStartAndPeriodEnd(UUID userId, LocalDate start, LocalDate end);
}
