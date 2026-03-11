package com.revconnect.auth.service;

import com.revconnect.auth.entity.RefreshToken;
import com.revconnect.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", 604800000L);
    }

    @Test
    @DisplayName("createRefreshToken - should delete existing token and save new one")
    void createRefreshToken_deletesExistingAndSavesNew() {
        Long userId = 1L;
        RefreshToken existing = buildToken(userId, Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        verify(refreshTokenRepository).delete(existing);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiryDate()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("createRefreshToken - should create token when none exists")
    void createRefreshToken_noExisting_createsNew() {
        Long userId = 5L;
        when(refreshTokenRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        verify(refreshTokenRepository, never()).delete(any());
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("verifyExpiration - valid non-expired token should pass through")
    void verifyExpiration_validToken_returnsToken() {
        RefreshToken token = buildToken(1L, Instant.now().plusSeconds(3600));
        assertThat(refreshTokenService.verifyExpiration(token)).isEqualTo(token);
    }

    @Test
    @DisplayName("verifyExpiration - expired token should throw and be deleted")
    void verifyExpiration_expiredToken_throwsAndDeletes() {
        RefreshToken expired = buildToken(1L, Instant.now().minusSeconds(60));

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expired))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(expired);
    }

    @Test
    @DisplayName("findByToken - delegates to repository")
    void findByToken_delegatesToRepository() {
        RefreshToken token = buildToken(1L, Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByToken("abc");

        assertThat(result).isPresent().contains(token);
    }

    @Test
    @DisplayName("deleteByUserId - calls repository delete")
    void deleteByUserId_callsRepository() {
        refreshTokenService.deleteByUserId(3L);
        verify(refreshTokenRepository).deleteByUserId(3L);
    }

    private RefreshToken buildToken(Long userId, Instant expiry) {
        return RefreshToken.builder()
                .userId(userId)
                .token(java.util.UUID.randomUUID().toString())
                .expiryDate(expiry)
                .build();
    }
}
