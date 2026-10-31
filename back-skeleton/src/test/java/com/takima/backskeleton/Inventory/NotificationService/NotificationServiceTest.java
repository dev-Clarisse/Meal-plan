package com.takima.backskeleton.Inventory.NotificationService;

import com.takima.backskeleton.Inventory.DAO.NotificationRepository;
import com.takima.backskeleton.Inventory.DTO.NotificationResponse;
import com.takima.backskeleton.Inventory.models.Inventory;
import com.takima.backskeleton.Inventory.models.Notification;
import com.takima.backskeleton.Inventory.models.NotificationType;
import com.takima.backskeleton.Inventory.models.Unit;
import com.takima.backskeleton.User.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository repo;

    NotificationService service;

    UUID userId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    User user;

    @BeforeEach
    void setUp() {
        service = new NotificationService(repo);
        user = new User("user@test.com", "hash", Instant.now());
        ReflectionTestUtils.setField(user, "id", userId);
    }

    @Test
    void listForUser_shouldReturnMappedResponses() {
        Notification n = notification(user, "Lait", NotificationType.EXPIRING_SOON, "Expire bientôt");

        when(repo.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(n));

        List<NotificationResponse> result = service.listForUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(NotificationType.EXPIRING_SOON);
        assertThat(result.get(0).inventoryName()).isEqualTo("Lait");
        assertThat(result.get(0).read()).isFalse();
    }

    @Test
    void unreadCount_shouldDelegateToRepository() {
        when(repo.countByUserIdAndReadIsFalse(userId)).thenReturn(4L);

        assertThat(service.unreadCount(userId)).isEqualTo(4L);
    }

    @Test
    void markAsRead_shouldFlipReadFlag() {
        Notification n = notification(user, "Lait", NotificationType.EXPIRED, "Périmé");

        when(repo.findById(n.getId())).thenReturn(Optional.of(n));

        service.markAsRead(userId, n.getId());

        assertThat(n.isRead()).isTrue();
        verify(repo).save(n);
    }

    @Test
    void markAsRead_shouldThrowNotFoundIfBelongsToAnotherUser() {
        User other = new User("other@test.com", "hash", Instant.now());
        ReflectionTestUtils.setField(other, "id", otherUserId);
        Notification n = notification(other, "Lait", NotificationType.EXPIRED, "Périmé");

        when(repo.findById(n.getId())).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.markAsRead(userId, n.getId()))
            .isInstanceOf(ResponseStatusException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void markAsRead_shouldThrowNotFoundIfUnknown() {
        UUID unknownId = UUID.randomUUID();

        when(repo.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(userId, unknownId))
            .isInstanceOf(ResponseStatusException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void markAllAsRead_shouldUpdateOnlyUnread() {
        Notification unread1 = notification(user, "Lait", NotificationType.EXPIRING_SOON, "msg1");
        Notification unread2 = notification(user, "Yaourt", NotificationType.EXPIRED, "msg2");
        Notification alreadyRead = notification(user, "Riz", NotificationType.EXPIRED, "msg3");
        alreadyRead.setRead(true);

        when(repo.findAllByUserIdOrderByCreatedAtDesc(userId))
            .thenReturn(List.of(unread1, unread2, alreadyRead));

        service.markAllAsRead(userId);

        assertThat(unread1.isRead()).isTrue();
        assertThat(unread2.isRead()).isTrue();
        verify(repo, times(2)).save(any(Notification.class));
    }

    private Notification notification(User owner, String invName,
                                      NotificationType type, String msg) {
        Inventory inv = new Inventory(owner, invName, BigDecimal.ONE, Unit.PIECE, LocalDate.now());
        ReflectionTestUtils.setField(inv, "id", UUID.randomUUID());
        Notification n = new Notification(owner, inv, type, msg);
        ReflectionTestUtils.setField(n, "id", UUID.randomUUID());
        return n;
    }
}