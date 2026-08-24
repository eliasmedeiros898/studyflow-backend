package com.studyflow.goals;

import com.studyflow.goals.GoalModels.GoalView;
import com.studyflow.goals.GoalModels.UpdateGoalRequest;
import com.studyflow.users.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GoalService {
    public static final int DEFAULT_MINUTES = 600;
    public static final int DEFAULT_QUESTIONS = 100;
    public static final int DEFAULT_ACCURACY = 70;

    private final GoalRepository goals;
    private final UserAccountRepository users;

    public GoalService(GoalRepository goals, UserAccountRepository users) { this.goals = goals; this.users = users; }

    public GoalView current(UUID userId) {
        var period = currentPeriod(userId);
        return goals.findByUserIdAndPeriodStartAndPeriodEnd(userId, period.start(), period.end())
                .map(this::view).orElse(new GoalView(null, period.start(), period.end(),
                        DEFAULT_MINUTES, DEFAULT_QUESTIONS, DEFAULT_ACCURACY));
    }

    @Transactional
    public GoalView updateCurrent(UUID userId, UpdateGoalRequest input) {
        var period = currentPeriod(userId);
        var goal = goals.findByUserIdAndPeriodStartAndPeriodEnd(userId, period.start(), period.end())
                .orElseGet(() -> new GoalEntity(users.getReferenceById(userId), period.start(), period.end(),
                        input.targetMinutes(), input.targetQuestions(), input.targetAccuracy()));
        goal.update(input.targetMinutes(), input.targetQuestions(), input.targetAccuracy());
        return view(goals.save(goal));
    }

    private Period currentPeriod(UUID userId) {
        var user = users.findById(userId).orElseThrow(() -> new NoSuchElementException("Conta não encontrada."));
        LocalDate today = LocalDate.now(ZoneId.of(user.getTimezone()));
        LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new Period(start, start.plusDays(6));
    }

    private GoalView view(GoalEntity goal) {
        return new GoalView(goal.getId(), goal.getPeriodStart(), goal.getPeriodEnd(), goal.getTargetMinutes(),
                goal.getTargetQuestions(), goal.getTargetAccuracy());
    }

    private record Period(LocalDate start, LocalDate end) {}
}
