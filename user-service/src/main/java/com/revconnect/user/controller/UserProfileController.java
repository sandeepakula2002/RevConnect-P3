package com.revconnect.user.controller;

import com.revconnect.user.dto.UserProfileRequest;
import com.revconnect.user.dto.UserProfileResponse;
import com.revconnect.user.dto.UserSummaryResponse;
import com.revconnect.user.dto.SyncUserRequest;
import com.revconnect.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
            @RequestHeader("X-User-Email") String userEmail) {

        log.debug("Fetching profile for email={}", userEmail);

        UserProfileResponse profile = userProfileService.getProfile(userEmail);

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody UserProfileRequest request) {

        log.info("Updating profile for email={}", userEmail);

        UserProfileResponse profile = userProfileService.updateProfile(userEmail, request);

        log.info("Profile updated for email={}", userEmail);

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserSummaryResponse> getUserById(@PathVariable Long userId) {

        log.debug("Fetching user summary for userId={}", userId);

        UserSummaryResponse user = userProfileService.getUserById(userId);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/sync")
    public ResponseEntity<UserSummaryResponse> syncUser(@Valid @RequestBody SyncUserRequest request) {

        log.info("Syncing user: userId={}, email={}", request.getId(), request.getEmail());

        UserSummaryResponse user = userProfileService.syncUser(request);

        log.info("User synced: userId={}", user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {

        log.debug("Fetching all users");

        List<UserSummaryResponse> users = userProfileService.getAllUsers();

        log.debug("Returned {} users", users.size());

        return ResponseEntity.ok(users);
    }
}