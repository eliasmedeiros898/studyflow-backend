package com.studyflow.notifications;

import com.studyflow.notifications.NotificationModels.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }

    @GetMapping
    public NotificationCenter center(@AuthenticationPrincipal Jwt jwt) { return service.center(userId(jwt)); }

    @PatchMapping("/{id}/read")
    public Notification read(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.markRead(userId(jwt), id);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readAll(@AuthenticationPrincipal Jwt jwt) { service.markAllRead(userId(jwt)); }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
