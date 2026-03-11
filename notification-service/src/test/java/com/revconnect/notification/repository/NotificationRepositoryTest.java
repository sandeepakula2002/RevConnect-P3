package com.revconnect.notification.repository;

import com.revconnect.notification.entity.Notification;
import com.revconnect.notification.entity.NotificationType;
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
@DisplayName("NotificationRepository Integration Tests")
class NotificationRepositoryTest {

    @Autowired NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        notificationRepository.save(Notification.builder().userId(10L)
                .type(NotificationType.LIKE).message("Like 1").isRead(false).build());
        notificationRepository.save(Notification.builder().userId(10L)
                .type(NotificationType.COMMENT).message("Comment 1").isRead(false).build());
        notificationRepository.save(Notification.builder().userId(10L)
                .type(NotificationType.CONNECTION_REQUEST).message("Connection request").isRead(true).build());
        notificationRepository.save(Notification.builder().userId(20L)
                .type(NotificationType.LIKE).message("Like for user 20").isRead(false).build());
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc - returns all notifications for user")
    void findByUserId_returnsAllForUser() {
        List<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(10L);
        assertThat(result).hasSize(3);
        result.forEach(n -> assertThat(n.getUserId()).isEqualTo(10L));
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc - different user returns only their notifications")
    void findByUserId_separatesUsers() {
        List<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(20L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("Like for user 20");
    }

    @Test
    @DisplayName("findByUserIdAndIsRead - returns only unread notifications")
    void findByUserIdAndIsRead_unread_returnsUnread() {
        List<Notification> result = notificationRepository.findByUserIdAndIsRead(10L, false);
        assertThat(result).hasSize(2);
        result.forEach(n -> assertThat(n.getIsRead()).isFalse());
    }

    @Test
    @DisplayName("findByUserIdAndIsRead - returns only read notifications")
    void findByUserIdAndIsRead_read_returnsRead() {
        List<Notification> result = notificationRepository.findByUserIdAndIsRead(10L, true);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsRead()).isTrue();
    }

    @Test
    @DisplayName("countByUserIdAndIsRead - returns correct unread count")
    void countByUserIdAndIsRead_returnsCorrectCount() {
        assertThat(notificationRepository.countByUserIdAndIsRead(10L, false)).isEqualTo(2L);
        assertThat(notificationRepository.countByUserIdAndIsRead(10L, true)).isEqualTo(1L);
    }

    @Test
    @DisplayName("save - persists notification with createdAt timestamp")
    void save_persistsWithTimestamp() {
        Notification saved = notificationRepository.save(
                Notification.builder().userId(99L).type(NotificationType.LIKE)
                        .message("Test").isRead(false).build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
