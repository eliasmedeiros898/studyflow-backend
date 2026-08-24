package com.studyflow.auth;

import com.studyflow.users.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtTokenService {
    private final JwtEncoder encoder;
    private final Duration tokenDuration;

    public JwtTokenService(JwtEncoder encoder,
                           @Value("${studyflow.security.token-duration}") Duration tokenDuration) {
        this.encoder = encoder;
        this.tokenDuration = tokenDuration;
    }

    public String issue(UserAccount user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("studyflow-api")
                .issuedAt(now)
                .expiresAt(now.plus(tokenDuration))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("version", user.getTokenVersion())
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    public long expiresInSeconds() { return tokenDuration.toSeconds(); }
}
