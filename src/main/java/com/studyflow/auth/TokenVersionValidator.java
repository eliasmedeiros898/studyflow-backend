package com.studyflow.auth;

import com.studyflow.users.UserAccountRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class TokenVersionValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error INVALID = new OAuth2Error("invalid_token", "Sessão revogada.", null);
    private final UserAccountRepository users;

    public TokenVersionValidator(UserAccountRepository users) { this.users = users; }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        try {
            Number version = jwt.getClaim("version");
            boolean valid = version != null && users.findById(UUID.fromString(jwt.getSubject()))
                    .map(user -> user.getTokenVersion() == version.intValue()).orElse(false);
            return valid ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(INVALID);
        } catch (RuntimeException exception) {
            return OAuth2TokenValidatorResult.failure(INVALID);
        }
    }
}
