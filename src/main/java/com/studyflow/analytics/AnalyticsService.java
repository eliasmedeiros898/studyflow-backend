package com.studyflow.analytics;

import com.studyflow.analytics.AnalyticsModels.*;
import com.studyflow.studies.SessionEntity;
import com.studyflow.studies.SessionRepository;
import com.studyflow.studies.StudyService;
import com.studyflow.studies.SubjectRepository;
import com.studyflow.users.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {
    private final SessionRepository sessions;
    private final SubjectRepository subjects;
    private final UserAccountRepository users;

    public AnalyticsService(SessionRepository sessions, SubjectRepository subjects, UserAccountRepository users) {
        this.sessions = sessions; this.subjects = subjects; this.users = users;
    }

    public PerformanceView performance(UUID userId, Period period) {
        LocalDate today = currentDate(userId);
        Range range = range(period, today);
        List<SessionEntity> current = sessions.findByUserIdAndStudiedOnBetween(userId, range.start(), range.end());
        long days = range.end().toEpochDay() - range.start().toEpochDay() + 1;
        Range previousRange = new Range(range.start().minusDays(days), range.start().minusDays(1));
        List<SessionEntity> previous = sessions.findByUserIdAndStudiedOnBetween(userId, previousRange.start(), previousRange.end());
        int minutes = sumMinutes(current);
        int questions = current.stream().mapToInt(SessionEntity::getQuestions).sum();
        int correct = current.stream().mapToInt(SessionEntity::getCorrectAnswers).sum();
        return new PerformanceView(period, range.start(), range.end(), minutes, questions, correct,
                StudyService.calculateAccuracy(correct, questions), sumMinutes(previous), timeline(period, range, current),
                subjectPerformance(userId, current, minutes));
    }

    private List<TimelinePoint> timeline(Period period, Range range, List<SessionEntity> found) {
        if (period == Period.YEAR) {
            List<TimelinePoint> points = new ArrayList<>();
            for (int month = 1; month <= 12; month++) {
                final int selectedMonth = month;
                var monthSessions = found.stream().filter(item -> item.getStudiedOn().getMonthValue() == selectedMonth).toList();
                LocalDate date = LocalDate.of(range.start().getYear(), month, 1);
                points.add(new TimelinePoint(date.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")),
                        date, sumMinutes(monthSessions), monthSessions.stream().mapToInt(SessionEntity::getQuestions).sum()));
            }
            return points;
        }
        List<TimelinePoint> points = new ArrayList<>();
        for (LocalDate date = range.start(); !date.isAfter(range.end()); date = date.plusDays(1)) {
            final LocalDate selectedDate = date;
            var daySessions = found.stream().filter(item -> item.getStudiedOn().equals(selectedDate)).toList();
            String label = period == Period.WEEK
                    ? date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR"))
                    : String.format("%02d", date.getDayOfMonth());
            points.add(new TimelinePoint(label, date, sumMinutes(daySessions),
                    daySessions.stream().mapToInt(SessionEntity::getQuestions).sum()));
        }
        return points;
    }

    private List<SubjectPerformance> subjectPerformance(UUID userId, List<SessionEntity> found, int totalMinutes) {
        return subjects.findByUserIdAndArchivedFalseAndNameContainingIgnoreCaseOrderByName(userId, "").stream()
                .map(subject -> {
                    var related = found.stream().filter(session -> session.getSubject().getId().equals(subject.getId())).toList();
                    int minutes = sumMinutes(related);
                    int questions = related.stream().mapToInt(SessionEntity::getQuestions).sum();
                    int correct = related.stream().mapToInt(SessionEntity::getCorrectAnswers).sum();
                    int share = totalMinutes == 0 ? 0 : (int) Math.round(minutes * 100.0 / totalMinutes);
                    return new SubjectPerformance(subject.getId(), subject.getName(), subject.getColor(), minutes,
                            questions, correct, StudyService.calculateAccuracy(correct, questions), share);
                }).sorted(Comparator.comparingInt(SubjectPerformance::minutes).reversed()).toList();
    }

    private Range range(Period period, LocalDate today) {
        return switch (period) {
            case WEEK -> {
                var start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new Range(start, start.plusDays(6));
            }
            case MONTH -> new Range(today.withDayOfMonth(1), today.with(TemporalAdjusters.lastDayOfMonth()));
            case YEAR -> new Range(today.withDayOfYear(1), today.with(TemporalAdjusters.lastDayOfYear()));
        };
    }

    private LocalDate currentDate(UUID userId) {
        var user = users.findById(userId).orElseThrow(() -> new NoSuchElementException("Conta não encontrada."));
        return LocalDate.now(ZoneId.of(user.getTimezone()));
    }

    private int sumMinutes(List<SessionEntity> items) { return items.stream().mapToInt(SessionEntity::getDurationMinutes).sum(); }
    private record Range(LocalDate start, LocalDate end) {}
}
