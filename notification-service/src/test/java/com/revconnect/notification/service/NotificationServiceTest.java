package com.revconnect.notification.service;

import com.revconnect.notification.dto.CreateNotificationRequest;
import com.revconnect.notification.dto.NotificationCountResponse;
import com.revconnect.notification.dto.NotificationResponse;
import com.revconnect.notification.entity.Notification;
import com.revconnect.notification.entity.NotificationType;
import com.revconnect.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @InjectMocks private NotificationService notificationService;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = Notification.builder()
                .id(1L)
                .userId(10L)
                .type(NotificationType.LIKE)
                .message("Your post was liked")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createNotification - saves and returns notification response")
    void createNotification_savesAndReturnsResponse() {
        CreateNotificationRequest req = CreateNotificationRequest.builder()
                .userId(10L)
                .type(NotificationType.LIKE)
                .message("Your post was liked")
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        NotificationResponse response = notificationService.createNotification(req);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getMessage()).isEqualTo("Your post was liked");
        assertThat(response.getIsRead()).isFalse();
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("getNotifications - returns ordered list for user")
    void getNotifications_returnsListForUser() {
        Notification n2 = Notification.builder()
                .id(2L).userId(10L).type(NotificationType.COMMENT)
                .message("Someone commented").isRead(false)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(sampleNotification, n2));

        List<NotificationResponse> result = notificationService.getNotifications(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getNotifications - returns empty list when no notifications")
    void getNotifications_noNotifications_returnsEmpty() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());

        assertThat(notificationService.getNotifications(10L)).isEmpty();
    }

    @Test
    @DisplayName("markAsRead - sets isRead to true and returns updated response")
    void markAsRead_setsReadTrue() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(1L);

        assertThat(response.getIsRead()).isTrue();
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("markAsRead - not found throws RuntimeException")
    void markAsRead_notFound_throwsException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notification not found");
    }

    @Test
    @DisplayName("markAllAsRead - marks all unread notifications for user as read")
    void markAllAsRead_marksAllUnread() {
        Notification unread1 = Notification.builder().id(1L).userId(10L)
                .type(NotificationType.LIKE).message("Like").isRead(false).build();
        Notification unread2 = Notification.builder().id(2L).userId(10L)
                .type(NotificationType.COMMENT).message("Comment").isRead(false).build();

        when(notificationRepository.findByUserIdAndIsRead(10L, false))
                .thenReturn(List.of(unread1, unread2));

        notificationService.markAllAsRead(10L);

        assertThat(unread1.getIsRead()).isTrue();
        assertThat(unread2.getIsRead()).isTrue();
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("markAllAsRead - no unread notifications does nothing harmful")
    void markAllAsRead_noUnread_doesNothing() {
        when(notificationRepository.findByUserIdAndIsRead(10L, false)).thenReturn(List.of());

        notificationService.markAllAsRead(10L);

        verify(notificationRepository).saveAll(List.of());
    }

    @Test
    @DisplayName("getUnreadCount - returns correct total and unread counts")
    void getUnreadCount_returnsCorrectCounts() {
        when(notificationRepository.countByUserIdAndIsRead(10L, false)).thenReturn(3L);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(sampleNotification, sampleNotification, sampleNotification,
                        Notification.builder().id(4L).userId(10L).type(NotificationType.LIKE)
                                .message("Read").isRead(true).build()));

        NotificationCountResponse response = notificationService.getUnreadCount(10L);

        assertThat(response.getUnread()).isEqualTo(3L);
        assertThat(response.getTotal()).isEqualTo(4L);
    }

    @Test
    @DisplayName("deleteNotification - deletes existing notification")
    void deleteNotification_existingId_deletes() {
        when(notificationRepository.existsById(1L)).thenReturn(true);

        notificationService.deleteNotification(1L);

        verify(notificationRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteNotification - not found throws RuntimeException")
    void deleteNotification_notFound_throwsException() {
        when(notificationRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> notificationService.deleteNotification(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notification not found");
    }
}
