package com.studyflow.auth;

import com.studyflow.auth.AuthModels.ForgotPasswordRequest;
import com.studyflow.auth.AuthModels.LoginRequest;
import com.studyflow.users.UserAccount;
import com.studyflow.users.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserAccountRepository users;
    @Mock PasswordEncoder passwords;
    @Mock JwtTokenService jwtTokens;
    @Mock PasswordResetTokenRepository resetTokens;
    @Mock PasswordResetDeliveryService delivery;
    AuthService service;

    @BeforeEach
    void setup() {
        service = new AuthService(users, passwords, jwtTokens, resetTokens, delivery,
                Duration.ofMinutes(30), Duration.ofMinutes(15), 2, true);
    }

    @Test
    void blocksAccountAfterMaximumFailedAttempts() {
        var user = new UserAccount("Elias", "elias@example.com", "hash", "America/Sao_Paulo");
        when(users.findByEmailIgnoreCase("elias@example.com")).thenReturn(Optional.of(user));
        when(passwords.matches("wrong", "hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("elias@example.com", "wrong")));
        assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("elias@example.com", "wrong")));

        assertEquals(2, user.getFailedLoginAttempts());
        assertNotNull(user.getLockedUntil());
    }

    @Test
    void keepsForgotPasswordResponseNeutralForUnknownEmail() {
        when(users.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        var response = service.forgotPassword(new ForgotPasswordRequest("unknown@example.com"));

        assertNull(response.developmentToken());
        assertTrue(response.message().startsWith("Se existir"));
        verify(resetTokens, never()).save(any());
    }

    @Test
    void exposesTokenOnlyForLocalDevelopmentWhenEmailIsUnavailable() {
        var user = new UserAccount("Elias", "elias@example.com", "hash", "America/Sao_Paulo");
        when(users.findByEmailIgnoreCase("elias@example.com")).thenReturn(Optional.of(user));
        when(resetTokens.findAllByUserIdAndUsedAtIsNull(user.getId())).thenReturn(List.of());
        when(delivery.deliver(eq("elias@example.com"), any())).thenReturn(false);

        var response = service.forgotPassword(new ForgotPasswordRequest("elias@example.com"));

        assertNotNull(response.developmentToken());
        assertTrue(response.developmentToken().length() >= 40);
        verify(resetTokens).save(any(PasswordResetToken.class));
    }
}
