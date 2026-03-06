package com.revconnect.interaction.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/{userId}")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserDetailsFallback")
    @Retry(name = "userService")
    Map<String, Object> getUserDetails(@PathVariable Long userId);

    default Map<String, Object> getUserDetailsFallback(Long userId, Exception e) {
        return Map.of(
            "id", userId,
            "username", "Unknown User",
            "error", "User service unavailable"
        );
    }
}
