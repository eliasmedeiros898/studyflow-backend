package com.studyflow.goals;

import com.studyflow.goals.GoalModels.GoalView;
import com.studyflow.goals.GoalModels.UpdateGoalRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/goals/current")
public class GoalController {
    private final GoalService service;

    public GoalController(GoalService service) { this.service = service; }

    @GetMapping
    public GoalView current(@AuthenticationPrincipal Jwt jwt) { return service.current(userId(jwt)); }

    @PutMapping
    public GoalView update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateGoalRequest input) {
        return service.updateCurrent(userId(jwt), input);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
