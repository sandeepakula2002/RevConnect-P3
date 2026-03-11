package com.revconnect.network.service;

import com.revconnect.network.entity.Follow;
import com.revconnect.network.repository.FollowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowService {

    private static final Logger log = LoggerFactory.getLogger(FollowService.class);

    private final FollowRepository followRepository;

    public FollowService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    @Transactional
    public Follow follow(Long followerId, Long followingId) {
        log.debug("follow() followerId={}, followingId={}", followerId, followingId);
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            log.warn("Already following: followerId={}, followingId={}", followerId, followingId);
            throw new IllegalStateException("Already following this user");
        }
        Follow saved = followRepository.save(new Follow(followerId, followingId));
        log.info("New follow saved: id={}", saved.getId());
        return saved;
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        log.debug("unfollow() followerId={}, followingId={}", followerId, followingId);
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        log.info("Unfollowed: followerId={}, followingId={}", followerId, followingId);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    public long getFollowerCount(Long userId) {
        long count = followRepository.countByFollowingId(userId);
        log.debug("getFollowerCount userId={}: {}", userId, count);
        return count;
    }

    public long getFollowingCount(Long userId) {
        long count = followRepository.countByFollowerId(userId);
        log.debug("getFollowingCount userId={}: {}", userId, count);
        return count;
    }

    public List<Follow> getFollowers(Long userId) {
        return followRepository.findByFollowingId(userId);
    }

    public List<Follow> getFollowing(Long userId) {
        return followRepository.findByFollowerId(userId);
    }
}
