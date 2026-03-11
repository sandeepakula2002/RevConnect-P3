package com.revconnect.network.repository;

import com.revconnect.network.entity.Follow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FollowRepository Integration Tests")
class FollowRepositoryTest {

    @Autowired FollowRepository followRepository;

    @BeforeEach
    void setUp() {
        followRepository.deleteAll();
        // user 1 follows user 2 and 3
        followRepository.save(new Follow(1L, 2L));
        followRepository.save(new Follow(1L, 3L));
        // user 4 follows user 2
        followRepository.save(new Follow(4L, 2L));
    }

    @Test
    @DisplayName("existsByFollowerIdAndFollowingId - returns true when follow exists")
    void existsByFollowerIdAndFollowingId_exists_returnsTrue() {
        assertThat(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).isTrue();
    }

    @Test
    @DisplayName("existsByFollowerIdAndFollowingId - returns false when follow does not exist")
    void existsByFollowerIdAndFollowingId_notExists_returnsFalse() {
        assertThat(followRepository.existsByFollowerIdAndFollowingId(2L, 1L)).isFalse();
    }

    @Test
    @DisplayName("findByFollowerId - returns all users that follower follows")
    void findByFollowerId_returnsFollowing() {
        List<Follow> result = followRepository.findByFollowerId(1L);
        assertThat(result).hasSize(2);
        result.forEach(f -> assertThat(f.getFollowerId()).isEqualTo(1L));
    }

    @Test
    @DisplayName("findByFollowingId - returns all followers for a user")
    void findByFollowingId_returnsFollowers() {
        List<Follow> result = followRepository.findByFollowingId(2L);
        assertThat(result).hasSize(2);
        result.forEach(f -> assertThat(f.getFollowingId()).isEqualTo(2L));
    }

    @Test
    @DisplayName("countByFollowingId - returns correct follower count")
    void countByFollowingId_returnsCorrectCount() {
        assertThat(followRepository.countByFollowingId(2L)).isEqualTo(2L);
        assertThat(followRepository.countByFollowingId(3L)).isEqualTo(1L);
    }

    @Test
    @DisplayName("countByFollowerId - returns correct following count")
    void countByFollowerId_returnsCorrectCount() {
        assertThat(followRepository.countByFollowerId(1L)).isEqualTo(2L);
        assertThat(followRepository.countByFollowerId(4L)).isEqualTo(1L);
    }

    @Test
    @DisplayName("deleteByFollowerIdAndFollowingId - removes the follow relationship")
    void deleteByFollowerIdAndFollowingId_removesFollow() {
        followRepository.deleteByFollowerIdAndFollowingId(1L, 2L);

        assertThat(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).isFalse();
        assertThat(followRepository.countByFollowerId(1L)).isEqualTo(1L); // still follows user 3
    }

    @Test
    @DisplayName("save - persists follow with timestamps and enforces unique constraint")
    void save_persistsWithTimestamps() {
        Follow saved = followRepository.save(new Follow(10L, 20L));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
