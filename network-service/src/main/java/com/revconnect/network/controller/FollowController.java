package com.revconnect.network.controller;

import com.revconnect.network.entity.Follow;
import com.revconnect.network.service.FollowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/network")
public class FollowController {

    private static final Logger log = LoggerFactory.getLogger(FollowController.class);

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/follow/{userId}")
    public ResponseEntity<Map<String, Object>> follow(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long userId) {
        log.info("Follow request: followerId={} -> followingId={}", currentUserId, userId);
        Follow follow = followService.follow(currentUserId, userId);
        log.info("Follow created: id={}, followerId={}, followingId={}", follow.getId(), currentUserId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", follow.getId(),
                "followerId", follow.getFollowerId(),
                "followingId", follow.getFollowingId(),
                "createdAt", follow.getCreatedAt().toString()
        ));
    }

    @DeleteMapping("/follow/{userId}")
    public ResponseEntity<Void> unfollow(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long userId) {
        log.info("Unfollow request: followerId={} -> followingId={}", currentUserId, userId);
        followService.unfollow(currentUserId, userId);
        log.info("Unfollow successful: followerId={}, followingId={}", currentUserId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/is-following/{userId}")
    public ResponseEntity<Boolean> isFollowing(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long userId) {
        boolean result = followService.isFollowing(currentUserId, userId);
        log.debug("isFollowing check: followerId={}, followingId={}, result={}", currentUserId, userId, result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/follower-count/{userId}")
    public ResponseEntity<Long> getFollowerCount(@PathVariable Long userId) {
        long count = followService.getFollowerCount(userId);
        log.debug("Follower count for userId={}: {}", userId, count);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/following-count/{userId}")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        long count = followService.getFollowingCount(userId);
        log.debug("Following count for userId={}: {}", userId, count);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getFollowers(@PathVariable Long userId) {
        log.debug("Fetching followers for userId={}", userId);
        List<Map<String, Object>> followers = followService.getFollowers(userId).stream()
                .map(f -> Map.<String, Object>of(
                        "id", f.getId(),
                        "followerId", f.getFollowerId(),
                        "followingId", f.getFollowingId(),
                        "createdAt", f.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());
        log.debug("Returned {} followers for userId={}", followers.size(), userId);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getFollowing(@PathVariable Long userId) {
        log.debug("Fetching following list for userId={}", userId);
        List<Map<String, Object>> following = followService.getFollowing(userId).stream()
                .map(f -> Map.<String, Object>of(
                        "id", f.getId(),
                        "followerId", f.getFollowerId(),
                        "followingId", f.getFollowingId(),
                        "createdAt", f.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());
        log.debug("Returned {} following for userId={}", following.size(), userId);
        return ResponseEntity.ok(following);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Bad request in FollowController: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        log.warn("Conflict in FollowController: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
