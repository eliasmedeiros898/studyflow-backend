package com.studyflow.auth;

import com.studyflow.auth.AuthModels.*;
import com.studyflow.users.UserAccount;
import com.studyflow.users.UserAccountRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokens;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordResetDeliveryService resetDelivery;
    private final Duration resetDuration;
    private final Duration loginLockDuration;
    private final int maximumLoginAttempts;
    private final boolean exposeDevelopmentToken;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserAccountRepository users, PasswordEncoder passwordEncoder, JwtTokenService tokens,
                       PasswordResetTokenRepository resetTokens,
                       PasswordResetDeliveryService resetDelivery,
                       @Value("${studyflow.security.password-reset-duration:PT30M}") Duration resetDuration,
                       @Value("${studyflow.security.login-lock-duration:PT15M}") Duration loginLockDuration,
                       @Value("${studyflow.security.maximum-login-attempts:5}") int maximumLoginAttempts,
                       @Value("${studyflow.security.expose-development-reset-token:false}") boolean exposeDevelopmentToken) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.resetTokens = resetTokens;
        this.resetDelivery = resetDelivery;
        this.resetDuration = resetDuration;
        this.loginLockDuration = loginLockDuration;
        this.maximumLoginAttempts = maximumLoginAttempts;
        this.exposeDevelopmentToken = exposeDevelopmentToken;
    }

    @Transactional
    public AuthResponse register(RegisterRequest input) {
        String email = input.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Já existe uma conta com este e-mail.");
        }
        var user = users.save(new UserAccount(input.name().trim(), email,
                passwordEncoder.encode(input.password()), "America/Sao_Paulo"));
        return response(user);
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthResponse login(LoginRequest input) {
        var user = users.findByEmailIgnoreCase(input.email().trim())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos."));
        var now = OffsetDateTime.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new BadCredentialsException("Muitas tentativas. Aguarde alguns minutos e tente novamente.");
        }
        if (user.getLockedUntil() != null) user.clearFailedLogins();
        if (!passwordEncoder.matches(input.password(), user.getPasswordHash())) {
            user.registerFailedLogin(maximumLoginAttempts, now.plus(loginLockDuration));
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }
        user.clearFailedLogins();
        return response(user);
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest input) {
        resetTokens.deleteByExpiresAtBefore(OffsetDateTime.now());
        String message = "Se existir uma conta com este e-mail, enviaremos as instruções de recuperação.";
        var user = users.findByEmailIgnoreCase(input.email().trim());
        if (user.isEmpty()) return new ForgotPasswordResponse(message, null);

        resetTokens.findAllByUserIdAndUsedAtIsNull(user.get().getId()).forEach(PasswordResetToken::use);
        byte[] bytes = new byte[32]; secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        resetTokens.save(new PasswordResetToken(user.get(), hash(rawToken), OffsetDateTime.now().plus(resetDuration)));
        boolean delivered = resetDelivery.deliver(user.get().getEmail(), rawToken);
        return new ForgotPasswordResponse(message, exposeDevelopmentToken && !delivered ? rawToken : null);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest input) {
        var token = resetTokens.findByTokenHash(hash(input.token().trim()))
                .orElseThrow(() -> new IllegalArgumentException("Link de recuperação inválido ou expirado."));
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Link de recuperação inválido ou expirado.");
        }
        var user = token.getUser();
        if (passwordEncoder.matches(input.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("A nova senha precisa ser diferente da senha atual.");
        }
        user.changePassword(passwordEncoder.encode(input.newPassword()));
        resetTokens.findAllByUserIdAndUsedAtIsNull(user.getId()).forEach(PasswordResetToken::use);
    }

    @Transactional(readOnly = true)
    public UserView me(UUID userId) {
        return view(users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Conta não encontrada.")));
    }

    private AuthResponse response(UserAccount user) {
        return new AuthResponse(tokens.issue(user), tokens.expiresInSeconds(), view(user));
    }

    public static UserView view(UserAccount user) {
        return new UserView(user.getId(), user.getName(), user.getEmail(), user.getTimezone(),
                user.getTargetExamName(), user.getTargetExamDate());
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível.", exception);
        }
    }
}
