package com.revconnect.network.controller;

import com.revconnect.network.dto.ConnectionRequest;
import com.revconnect.network.dto.ConnectionResponse;
import com.revconnect.network.service.ConnectionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/network")
public class ConnectionController {

    private static final Logger log = LoggerFactory.getLogger(ConnectionController.class);

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/connect")
    public ResponseEntity<ConnectionResponse> sendConnectionRequest(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ConnectionRequest request) {
        log.info("Connection request from userId={} to userId={}", userId, request.getConnectedUserId());
        ConnectionResponse response = connectionService.sendRequest(userId, request);
        log.info("Connection request sent: connectionId={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/connections/{connectionId}/accept")
    public ResponseEntity<ConnectionResponse> acceptConnectionRequest(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long connectionId) {
        log.info("Accepting connectionId={} by userId={}", connectionId, userId);
        ConnectionResponse response = connectionService.acceptRequest(userId, connectionId);
        log.info("Connection accepted: connectionId={}", connectionId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/connections/{connectionId}/reject")
    public ResponseEntity<ConnectionResponse> rejectConnectionRequest(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long connectionId) {
        log.info("Rejecting connectionId={} by userId={}", connectionId, userId);
        ConnectionResponse response = connectionService.rejectRequest(userId, connectionId);
        log.info("Connection rejected: connectionId={}", connectionId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/connections/{connectionId}")
    public ResponseEntity<Void> deleteConnection(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long connectionId) {
        log.info("Deleting connectionId={} by userId={}", connectionId, userId);
        connectionService.deleteConnection(userId, connectionId);
        log.info("Connection deleted: connectionId={}", connectionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/connections")
    public ResponseEntity<List<ConnectionResponse>> getConnections(
            @RequestHeader("X-User-Id") Long userId) {
        log.debug("Fetching connections for userId={}", userId);
        List<ConnectionResponse> connections = connectionService.getConnections(userId);
        log.debug("Found {} connections for userId={}", connections.size(), userId);
        return ResponseEntity.ok(connections);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ConnectionResponse>> getPendingRequests(
            @RequestHeader("X-User-Id") Long userId) {
        log.debug("Fetching pending requests for userId={}", userId);
        List<ConnectionResponse> pendingRequests = connectionService.getPendingRequests(userId);
        log.debug("Found {} pending requests for userId={}", pendingRequests.size(), userId);
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/sent")
    public ResponseEntity<List<ConnectionResponse>> getSentRequests(
            @RequestHeader("X-User-Id") Long userId) {
        log.debug("Fetching sent requests for userId={}", userId);
        List<ConnectionResponse> sentRequests = connectionService.getSentRequests(userId);
        return ResponseEntity.ok(sentRequests);
    }

    @GetMapping("/check/{otherUserId}")
    public ResponseEntity<Map<String, Object>> checkConnection(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long otherUserId) {
        log.debug("Checking connection between userId={} and otherUserId={}", userId, otherUserId);
        Map<String, Object> connectionStatus = connectionService.checkConnection(userId, otherUserId);
        return ResponseEntity.ok(connectionStatus);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getConnectionCount(
            @RequestHeader("X-User-Id") Long userId) {
        Long count = connectionService.getConnectionCount(userId);
        log.debug("Connection count for userId={}: {}", userId, count);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        log.warn("Conflict: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Unexpected error in ConnectionController", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
    }
}
