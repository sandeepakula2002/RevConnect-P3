package com.revconnect.network.service;

import com.revconnect.network.entity.Follow;
import com.revconnect.network.repository.FollowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService Unit Tests")
class FollowServiceTest {

    @Mock private FollowRepository followRepository;
    @InjectMocks private FollowService followService;

    private Follow buildFollow(Long followerId, Long followingId) {
        Follow f = new Follow(followerId, followingId);
        // Simulate the id being set after save
        try {
            java.lang.reflect.Field idField = Follow.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(f, 1L);
            java.lang.reflect.Field tsField = Follow.class.getDeclaredField("createdAt");
            tsField.setAccessible(true);
            tsField.set(f, LocalDateTime.now());
        } catch (Exception ignored) {}
        return f;
    }

    @Test
    @DisplayName("follow - saves follow relationship and returns it")
    void follow_savesFollow() {
        Follow saved = buildFollow(1L, 2L);
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);
        when(followRepository.save(any(Follow.class))).thenReturn(saved);

        Follow result = followService.follow(1L, 2L);

        assertThat(result.getFollowerId()).isEqualTo(1L);
        assertThat(result.getFollowingId()).isEqualTo(2L);
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("follow - self-follow throws IllegalArgumentException")
    void follow_selfFollow_throwsException() {
        assertThatThrownBy(() -> followService.follow(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot follow yourself");
    }

    @Test
    @DisplayName("follow - already following throws IllegalStateException")
    void follow_alreadyFollowing_throwsException() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> followService.follow(1L, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Already following");
    }

    @Test
    @DisplayName("unfollow - delegates delete to repository")
    void unfollow_callsDelete() {
        followService.unfollow(1L, 2L);
        verify(followRepository).deleteByFollowerIdAndFollowingId(1L, 2L);
    }

    @Test
    @DisplayName("isFollowing - returns true when following")
    void isFollowing_whenFollowing_returnsTrue() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);
        assertThat(followService.isFollowing(1L, 2L)).isTrue();
    }

    @Test
    @DisplayName("isFollowing - returns false when not following")
    void isFollowing_whenNotFollowing_returnsFalse() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);
        assertThat(followService.isFollowing(1L, 2L)).isFalse();
    }

    @Test
    @DisplayName("getFollowerCount - returns correct count from repository")
    void getFollowerCount_returnsCount() {
        when(followRepository.countByFollowingId(5L)).thenReturn(42L);
        assertThat(followService.getFollowerCount(5L)).isEqualTo(42L);
    }

    @Test
    @DisplayName("getFollowingCount - returns correct count from repository")
    void getFollowingCount_returnsCount() {
        when(followRepository.countByFollowerId(5L)).thenReturn(17L);
        assertThat(followService.getFollowingCount(5L)).isEqualTo(17L);
    }

    @Test
    @DisplayName("getFollowers - returns list of follows where followingId matches")
    void getFollowers_returnsList() {
        Follow f = buildFollow(3L, 5L);
        when(followRepository.findByFollowingId(5L)).thenReturn(List.of(f));

        List<Follow> result = followService.getFollowers(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFollowerId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getFollowing - returns list of follows where followerId matches")
    void getFollowing_returnsList() {
        Follow f = buildFollow(5L, 7L);
        when(followRepository.findByFollowerId(5L)).thenReturn(List.of(f));

        List<Follow> result = followService.getFollowing(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFollowingId()).isEqualTo(7L);
    }
}
