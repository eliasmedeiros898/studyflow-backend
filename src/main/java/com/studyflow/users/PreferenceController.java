package com.studyflow.users;

import com.studyflow.users.PreferenceModels.PreferencesView;
import com.studyflow.users.PreferenceModels.UpdatePreferencesRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {
    private final PreferenceService service;

    public PreferenceController(PreferenceService service) { this.service = service; }

    @GetMapping
    public PreferencesView get(@AuthenticationPrincipal Jwt jwt) {
        return service.get(UUID.fromString(jwt.getSubject()));
    }

    @PutMapping
    public PreferencesView update(@AuthenticationPrincipal Jwt jwt,
                                  @Valid @RequestBody UpdatePreferencesRequest input) {
        return service.update(UUID.fromString(jwt.getSubject()), input);
    }
}
