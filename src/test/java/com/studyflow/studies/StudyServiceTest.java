package com.studyflow.studies;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyServiceTest {
    @Test
    void calculatesConsolidatedAccuracyFromTotals() {
        assertThat(StudyService.calculateAccuracy(28, 50)).isEqualTo(56);
    }

    @Test
    void returnsZeroWhenThereAreNoQuestions() {
        assertThat(StudyService.calculateAccuracy(0, 0)).isZero();
    }

    @Test
    void keepsTheLargestHistoricalStreak() {
        var dates = List.of(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 8), LocalDate.of(2026, 8, 9));

        assertThat(StudyService.calculateBestStreak(dates)).isEqualTo(3);
    }

    @Test
    void schedulesWeakerResultsSooner() {
        assertThat(StudyService.reviewIntervalDays(4, 10)).isEqualTo(1);
        assertThat(StudyService.reviewIntervalDays(6, 10)).isEqualTo(3);
        assertThat(StudyService.reviewIntervalDays(8, 10)).isEqualTo(7);
        assertThat(StudyService.reviewIntervalDays(9, 10)).isEqualTo(15);
        assertThat(StudyService.reviewIntervalDays(0, 0)).isZero();
    }

    @Test
    void acceptsReviewTimeOrQuestionsIndependently() {
        StudyService.validateReviewMetrics(45, 0, 0);
        StudyService.validateReviewMetrics(0, 20, 14);
    }

    @Test
    void rejectsInconsistentReviewAnswers() {
        assertThatThrownBy(() -> StudyService.validateReviewMetrics(30, 10, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acertos");
    }
}
