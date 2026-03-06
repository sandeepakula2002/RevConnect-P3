package com.revconnect.interaction.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendNotificationFallback")
    @Retry(name = "notificationService")
    void sendNotification(@RequestBody Map<String, Object> notification);

    default void sendNotificationFallback(Map<String, Object> notification, Exception e) {
        // Log the failure but don't throw exception - notifications are not critical
        System.err.println("Failed to send notification: " + e.getMessage());
    }
}
