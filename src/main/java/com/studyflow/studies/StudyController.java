package com.studyflow.studies;

import com.studyflow.studies.StudyModels.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class StudyController {
    private final StudyService service;

    public StudyController(StudyService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public Dashboard dashboard(@AuthenticationPrincipal Jwt jwt) { return service.dashboard(userId(jwt)); }

    @GetMapping("/subjects")
    public List<Subject> subjects(@AuthenticationPrincipal Jwt jwt,
                                  @RequestParam(required = false) String query,
                                  @RequestParam(defaultValue = "false") boolean includeArchived) {
        return service.listSubjects(userId(jwt), query, includeArchived);
    }

    @GetMapping("/subjects/metrics")
    public List<SubjectMetrics> subjectMetrics(@AuthenticationPrincipal Jwt jwt) {
        return service.subjectMetrics(userId(jwt));
    }

    @GetMapping("/subjects/{id}")
    public SubjectDetails subjectDetails(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.subjectDetails(userId(jwt), id);
    }

    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    public Subject createSubject(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateSubject input) {
        return service.createSubject(userId(jwt), input);
    }

    @PatchMapping("/subjects/{id}")
    public Subject updateSubject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                 @Valid @RequestBody UpdateSubject input) {
        return service.updateSubject(userId(jwt), id, input);
    }

    @PatchMapping("/subjects/{id}/archive")
    public Subject archiveSubject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.archiveSubject(userId(jwt), id);
    }

    @PatchMapping("/subjects/{id}/restore")
    public Subject restoreSubject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.restoreSubject(userId(jwt), id);
    }

    @GetMapping("/tasks")
    public List<StudyTask> tasks(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.listTasks(userId(jwt), date);
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public StudyTask createTask(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateTask input) {
        return service.createTask(userId(jwt), input);
    }

    @PatchMapping("/tasks/{id}/toggle")
    public StudyTask toggleTask(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.toggleTask(userId(jwt), id);
    }

    @PutMapping("/tasks/{id}")
    public StudyTask updateTask(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                @Valid @RequestBody UpdateTask input) {
        return service.updateTask(userId(jwt), id, input);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.deleteTask(userId(jwt), id);
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public StudySession createSession(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateSession input) {
        return service.createSession(userId(jwt), input);
    }

    @GetMapping("/sessions")
    public List<StudySession> sessions(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.listSessions(userId(jwt), subjectId, from, to);
    }

    @PutMapping("/sessions/{id}")
    public StudySession updateSession(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                      @Valid @RequestBody UpdateSession input) {
        return service.updateSession(userId(jwt), id, input);
    }

    @DeleteMapping("/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.deleteSession(userId(jwt), id);
    }

    @GetMapping("/reviews")
    public List<StudyTask> reviews(@AuthenticationPrincipal Jwt jwt) {
        return service.listAutomaticReviews(userId(jwt));
    }

    @GetMapping("/reviews/progress")
    public List<TopicProgress> reviewProgress(@AuthenticationPrincipal Jwt jwt) {
        return service.listReviewProgress(userId(jwt));
    }

    @PostMapping("/reviews/{id}/complete")
    public CompleteReviewResult completeReview(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                               @RequestBody CompleteReviewRequest input) {
        return service.completeReview(userId(jwt), id, input);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
