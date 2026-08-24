package com.studyflow.analytics;

import com.studyflow.analytics.AnalyticsModels.PerformanceView;
import com.studyflow.analytics.AnalyticsModels.Period;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/performance")
public class AnalyticsController {
    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) { this.service = service; }

    @GetMapping
    public PerformanceView performance(@AuthenticationPrincipal Jwt jwt,
                                       @RequestParam(defaultValue = "WEEK") Period period) {
        return service.performance(UUID.fromString(jwt.getSubject()), period);
    }
}
