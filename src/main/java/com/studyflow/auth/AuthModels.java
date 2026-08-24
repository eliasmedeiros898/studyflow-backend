package com.studyflow.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;
import java.time.LocalDate;

public final class AuthModels {
    private AuthModels() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Size(min = 8, max = 72) String password
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record ForgotPasswordRequest(@NotBlank @Email @Size(max = 180) String email) {}

    public record ForgotPasswordResponse(String message, String developmentToken) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 72) String newPassword
    ) {}

    public record MessageResponse(String message) {}

    public record UserView(UUID id, String name, String email, String timezone,
                           String targetExamName, LocalDate targetExamDate) {}
    public record AuthResponse(String accessToken, long expiresIn, UserView user) {}
}
