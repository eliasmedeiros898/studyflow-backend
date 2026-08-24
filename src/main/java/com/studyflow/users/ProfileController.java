package com.studyflow.users;

import com.studyflow.auth.AuthModels.UserView;
import com.studyflow.users.ProfileModels.UpdateProfileRequest;
import com.studyflow.users.ProfileModels.ChangePasswordRequest;
import com.studyflow.users.ProfileModels.ChangeEmailRequest;
import com.studyflow.auth.AuthModels.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService service;

    public ProfileController(ProfileService service) { this.service = service; }

    @PatchMapping
    public UserView update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest input) {
        return service.update(UUID.fromString(jwt.getSubject()), input);
    }

    @PutMapping("/password")
    public MessageResponse changePassword(@AuthenticationPrincipal Jwt jwt,
                                          @Valid @RequestBody ChangePasswordRequest input) {
        service.changePassword(UUID.fromString(jwt.getSubject()), input);
        return new MessageResponse("Senha alterada. Entre novamente para continuar.");
    }

    @PutMapping("/email")
    public UserView changeEmail(@AuthenticationPrincipal Jwt jwt,
                                @Valid @RequestBody ChangeEmailRequest input) {
        return service.changeEmail(UUID.fromString(jwt.getSubject()), input);
    }
}
