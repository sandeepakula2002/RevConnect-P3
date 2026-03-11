package com.revconnect.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "test-secret-key-minimum-256-bits-for-hmac-sha256-algorithm-here!!";
    private static final Long EXPIRATION = 86400000L; // 1 day

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    @Test
    @DisplayName("generateToken - should create a valid JWT for given userId and email")
    void generateToken_shouldReturnValidJwt() {
        String token = jwtService.generateToken(1L, "alice@example.com");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("validateToken - valid token should return true")
    void validateToken_validToken_returnsTrue() {
        String token = jwtService.generateToken(1L, "alice@example.com");
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken - tampered token should return false")
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtService.generateToken(1L, "alice@example.com");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtService.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("validateToken - empty string should return false")
    void validateToken_emptyString_returnsFalse() {
        assertThat(jwtService.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("extractUserId - should return correct userId embedded in token")
    void extractUserId_shouldReturnCorrectUserId() {
        String token = jwtService.generateToken(42L, "bob@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("extractEmail - should return correct email embedded in token")
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateToken(1L, "carol@example.com");
        assertThat(jwtService.extractEmail(token)).isEqualTo("carol@example.com");
    }

    @Test
    @DisplayName("generateToken - different users produce different tokens")
    void generateToken_differentUsers_produceDifferentTokens() {
        String token1 = jwtService.generateToken(1L, "user1@example.com");
        String token2 = jwtService.generateToken(2L, "user2@example.com");
        assertThat(token1).isNotEqualTo(token2);
    }
}
