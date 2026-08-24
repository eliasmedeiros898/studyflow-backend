package com.studyflow.users;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public final class ProfileModels {
    private ProfileModels() {}

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 80) String timezone,
            @NotBlank @Size(max = 120) String targetExamName,
            @NotNull @FutureOrPresent LocalDate targetExamDate
    ) {}

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 72) String newPassword
    ) {}

    public record ChangeEmailRequest(
            @NotBlank String currentPassword,
            @NotBlank @Email @Size(max = 180) String newEmail
    ) {}
}
