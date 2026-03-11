package com.revconnect.network.service;

import com.revconnect.network.client.NotificationClient;
import com.revconnect.network.client.UserClient;
import com.revconnect.network.dto.ConnectionRequest;
import com.revconnect.network.dto.ConnectionResponse;
import com.revconnect.network.entity.Connection;
import com.revconnect.network.entity.Connection.ConnectionStatus;
import com.revconnect.network.repository.ConnectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConnectionService Unit Tests")
class ConnectionServiceTest {

    @Mock private ConnectionRepository connectionRepository;
    @Mock private UserClient userClient;
    @Mock private NotificationClient notificationClient;

    @InjectMocks private ConnectionService connectionService;

    private Connection pendingConnection;

    @BeforeEach
    void setUp() {
        pendingConnection = Connection.builder()
                .id(1L).userId(1L).connectedUserId(2L)
                .status(ConnectionStatus.PENDING)
                .build();
        // inject timestamps
        try {
            java.lang.reflect.Field f = Connection.class.getDeclaredField("createdAt");
            f.setAccessible(true);
            f.set(pendingConnection, LocalDateTime.now());
        } catch (Exception ignored) {}
    }

    // ── sendRequest ───────────────────────────────────────────────────

    @Test
    @DisplayName("sendRequest - creates PENDING connection between two users")
    void sendRequest_createsPendingConnection() {
        ConnectionRequest req = ConnectionRequest.builder().connectedUserId(2L).build();

        when(connectionRepository.findConnectionBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(connectionRepository.save(any(Connection.class))).thenReturn(pendingConnection);
        when(userClient.getUserDetails(anyLong())).thenReturn(new ConnectionResponse.UserDetails());

        ConnectionResponse response = connectionService.sendRequest(1L, req);

        assertThat(response.getStatus()).isEqualTo(ConnectionStatus.PENDING);
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(connectionRepository).save(any(Connection.class));
    }

    @Test
    @DisplayName("sendRequest - self-connection throws IllegalArgumentException")
    void sendRequest_selfConnection_throwsException() {
        ConnectionRequest req = ConnectionRequest.builder().connectedUserId(1L).build();

        assertThatThrownBy(() -> connectionService.sendRequest(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yourself");
    }

    @Test
    @DisplayName("sendRequest - duplicate connection throws IllegalStateException")
    void sendRequest_duplicateConnection_throwsException() {
        ConnectionRequest req = ConnectionRequest.builder().connectedUserId(2L).build();
        when(connectionRepository.findConnectionBetweenUsers(1L, 2L)).thenReturn(Optional.of(pendingConnection));

        assertThatThrownBy(() -> connectionService.sendRequest(1L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
    }

    // ── acceptRequest ─────────────────────────────────────────────────

    @Test
    @DisplayName("acceptRequest - recipient accepts and status becomes ACCEPTED")
    void acceptRequest_recipientAccepts_statusAccepted() {
        when(connectionRepository.findById(1L)).thenReturn(Optional.of(pendingConnection));
        when(connectionRepository.save(any(Connection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userClient.getUserDetails(anyLong())).thenReturn(new ConnectionResponse.UserDetails());

        ConnectionResponse response = connectionService.acceptRequest(2L, 1L);

        assertThat(response.getStatus()).isEqualTo(ConnectionStatus.ACCEPTED);
        verify(connectionRepository).save(any(Connection.class));
    }

    @Test
    @DisplayName("acceptRequest - non-recipient throws IllegalArgumentException")
    void acceptRequest_nonRecipient_throwsException() {
        when(connectionRepository.findById(1L)).thenReturn(Optional.of(pendingConnection));

        assertThatThrownBy(() -> connectionService.acceptRequest(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("acceptRequest - already accepted throws IllegalStateException")
    void acceptRequest_alreadyAccepted_throwsException() {
        pendingConnection.setStatus(ConnectionStatus.ACCEPTED);
        when(connectionRepository.findById(1L)).thenReturn(Optional.of(pendingConnection));

        assertThatThrownBy(() -> connectionService.acceptRequest(2L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in pending");
    }

    // ── rejectRequest ─────────────────────────────────────────────────

    @Test
    @DisplayName("rejectRequest - recipient rejects and status becomes REJECTED")
    void rejectRequest_recipientRejects_statusRejected() {
        when(connectionRepository.findById(1L)).thenReturn(Optional.of(pendingConnection));
        when(connectionRepository.save(any(Connection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userClient.getUserDetails(anyLong())).thenReturn(new ConnectionResponse.UserDetails());

        ConnectionResponse response = connectionService.rejectRequest(2L, 1L);

        assertThat(response.getStatus()).isEqualTo(ConnectionStatus.REJECTED);
    }

    // ── deleteConnection ──────────────────────────────────────────────

    @Test
    @DisplayName("deleteConnection - initiator can delete connection")
    void deleteConnection_initiatorDeletes() {
        pendingConnection.setStatus(ConnectionStatus.ACCEPTED);
        when(connectionRepository.findById(1L)).thenReturn(Optional.of(pendingConnection));

        connectionService.deleteConnection(1L, 1L);

        verify(connectionRepository).delete(pendingConnection);
    }

    @Test
    @DisplayName("deleteConnection - unrelated user throws IllegalArgumentException")
    void deleteConnection_unrelatedUser_throwsException() {
        when(connectionRepository.findById(1L)).thenReturn(Optional.of(pendingConnection));

        assertThatThrownBy(() -> connectionService.deleteConnection(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not authorized");
    }

    // ── checkConnection ───────────────────────────────────────────────

    @Test
    @DisplayName("checkConnection - returns NONE when no connection exists")
    void checkConnection_noConnection_returnsNone() {
        when(connectionRepository.findConnectionBetweenUsers(1L, 3L)).thenReturn(Optional.empty());

        Map<String, Object> result = connectionService.checkConnection(1L, 3L);

        assertThat(result.get("connected")).isEqualTo(false);
        assertThat(result.get("status")).isEqualTo("NONE");
    }

    @Test
    @DisplayName("checkConnection - accepted connection returns connected=true")
    void checkConnection_acceptedConnection_returnsConnected() {
        pendingConnection.setStatus(ConnectionStatus.ACCEPTED);
        when(connectionRepository.findConnectionBetweenUsers(1L, 2L)).thenReturn(Optional.of(pendingConnection));

        Map<String, Object> result = connectionService.checkConnection(1L, 2L);

        assertThat(result.get("connected")).isEqualTo(true);
        assertThat(result.get("status")).isEqualTo("ACCEPTED");
    }

    // ── getConnectionCount ────────────────────────────────────────────

    @Test
    @DisplayName("getConnectionCount - delegates to repository")
    void getConnectionCount_returnsCount() {
        when(connectionRepository.countAcceptedConnections(1L)).thenReturn(5L);
        assertThat(connectionService.getConnectionCount(1L)).isEqualTo(5L);
    }
}
