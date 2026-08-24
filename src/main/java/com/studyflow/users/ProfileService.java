package com.studyflow.users;

import com.studyflow.auth.AuthModels.UserView;
import com.studyflow.auth.AuthService;
import com.studyflow.users.ProfileModels.UpdateProfileRequest;
import com.studyflow.users.ProfileModels.ChangePasswordRequest;
import com.studyflow.users.ProfileModels.ChangeEmailRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProfileService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserView update(UUID userId, UpdateProfileRequest input) {
        try {
            ZoneId.of(input.timezone());
        } catch (ZoneRulesException exception) {
            throw new IllegalArgumentException("Fuso horário inválido.");
        }
        var user = users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Conta não encontrada."));
        user.updateProfile(input.name().trim(), input.timezone(), input.targetExamName().trim(), input.targetExamDate());
        return new UserView(user.getId(), user.getName(), user.getEmail(), user.getTimezone(),
                user.getTargetExamName(), user.getTargetExamDate());
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest input) {
        var user = findUser(userId);
        requireCurrentPassword(user, input.currentPassword());
        if (passwordEncoder.matches(input.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("A nova senha precisa ser diferente da senha atual.");
        }
        user.changePassword(passwordEncoder.encode(input.newPassword()));
    }

    @Transactional
    public UserView changeEmail(UUID userId, ChangeEmailRequest input) {
        var user = findUser(userId);
        requireCurrentPassword(user, input.currentPassword());
        String email = input.newEmail().trim().toLowerCase();
        if (!email.equalsIgnoreCase(user.getEmail()) && users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Já existe uma conta com este e-mail.");
        }
        user.changeEmail(email);
        return AuthService.view(user);
    }

    private UserAccount findUser(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new NoSuchElementException("Conta não encontrada."));
    }

    private void requireCurrentPassword(UserAccount user, String password) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Senha atual incorreta.");
        }
    }
}
