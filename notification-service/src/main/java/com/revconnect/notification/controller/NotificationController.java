package com.revconnect.notification.controller;

import com.revconnect.notification.dto.CreateNotificationRequest;
import com.revconnect.notification.dto.NotificationCountResponse;
import com.revconnect.notification.dto.NotificationResponse;
import com.revconnect.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Create notification
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {

        log.info("Creating notification: userId={}, type={}", request.getUserId(), request.getType());

        NotificationResponse response = notificationService.createNotification(request);

        log.info("Notification created: id={}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get notifications for user
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestHeader("X-User-Id") Long userId) {

        log.debug("Fetching notifications for userId={}", userId);

        List<NotificationResponse> notifications =
                notificationService.getNotifications(userId);

        log.debug("Returned {} notifications for userId={}", notifications.size(), userId);

        return ResponseEntity.ok(notifications);
    }

    // Mark notification as read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId) {

        log.info("Marking notificationId={} as read", notificationId);

        NotificationResponse response =
                notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(response);
    }

    // Mark all notifications as read
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Marking all notifications as read for userId={}", userId);

        notificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }

    // Get unread notification count
    @GetMapping("/count")
    public ResponseEntity<NotificationCountResponse> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {

        NotificationCountResponse countResponse =
                notificationService.getUnreadCount(userId);

        log.debug("Unread notification count for userId={}: {}",
                userId,
                countResponse.getUnread());

        return ResponseEntity.ok(countResponse);
    }

    // Delete notification
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId) {

        log.info("Deleting notificationId={}", notificationId);

        notificationService.deleteNotification(notificationId);

        return ResponseEntity.noContent().build();
    }
}