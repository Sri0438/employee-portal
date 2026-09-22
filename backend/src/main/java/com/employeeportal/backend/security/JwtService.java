package com.employeeportal.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

    // Local-development-only secret. In Phase 10 this moves to an environment variable.
    private final SecretKey key = Keys.hmacShaKeyFor(
        "change-this-to-a-long-random-secret-string-at-least-64-characters-long".getBytes()
    );

    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 8; // 8 hours

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
