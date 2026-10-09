package com.takima.backskeleton.Inventory.InventoryAlertScheduler;

import com.takima.backskeleton.Inventory.DAO.InventoryRepository;
import com.takima.backskeleton.Inventory.DAO.NotificationRepository;
import com.takima.backskeleton.Inventory.ExpirationAlertScheduler.ExpirationAlertScheduler;
import com.takima.backskeleton.Inventory.models.Inventory;
import com.takima.backskeleton.Inventory.models.Notification;
import com.takima.backskeleton.Inventory.models.NotificationType;
import com.takima.backskeleton.Inventory.models.Unit;
import com.takima.backskeleton.User.models.User;
import com.takima.backskeleton.config.InventoryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpirationAlertSchedulerTest {

    @Mock InventoryRepository inventoryRepo;
    @Mock NotificationRepository notifRepo;

    ExpirationAlertScheduler scheduler;

    User user;

    @BeforeEach
    void setUp() {
        scheduler = new ExpirationAlertScheduler(inventoryRepo, notifRepo, new InventoryProperties(3));
        user = new User("u@test.com", "hash", Instant.now());
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    }

    @Test
    void runDailyAlerts_shouldCreateNotificationForExpiredItem() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Inventory expired = inventory("Lait", yesterday);

        when(inventoryRepo.findAllByExpiryDateLessThanEqual(any(LocalDate.class)))
            .thenReturn(List.of(expired));
        when(notifRepo.existsByInventoryIdAndType(expired.getId(), NotificationType.EXPIRED))
            .thenReturn(false);

        scheduler.runDailyAlerts();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifRepo).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.EXPIRED);
        assertThat(saved.getMessage()).contains("Lait");
        assertThat(saved.getMessage()).contains("expiré");
    }

    @Test
    void runDailyAlerts_shouldCreateNotificationForExpiringSoonItem() {
        LocalDate soon = LocalDate.now().plusDays(2);
        Inventory item = inventory("Yaourt", soon);

        when(inventoryRepo.findAllByExpiryDateLessThanEqual(any(LocalDate.class)))
            .thenReturn(List.of(item));
        when(notifRepo.existsByInventoryIdAndType(item.getId(), NotificationType.EXPIRING_SOON))
            .thenReturn(false);

        scheduler.runDailyAlerts();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifRepo).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.EXPIRING_SOON);
    }

    @Test
    void runDailyAlerts_shouldSkipExistingNotification() {
        LocalDate soon = LocalDate.now().plusDays(2);
        Inventory item = inventory("Yaourt", soon);

        when(inventoryRepo.findAllByExpiryDateLessThanEqual(any(LocalDate.class)))
            .thenReturn(List.of(item));
        when(notifRepo.existsByInventoryIdAndType(item.getId(), NotificationType.EXPIRING_SOON))
            .thenReturn(true);   // déjà notifié → anti-doublon

        scheduler.runDailyAlerts();

        verify(notifRepo, never()).save(any());
    }

    @Test
    void runDailyAlerts_shouldIgnoreNullExpiryDate() {
        Inventory noDate = inventory("Riz", null);

        when(inventoryRepo.findAllByExpiryDateLessThanEqual(any(LocalDate.class)))
            .thenReturn(List.of(noDate));

        scheduler.runDailyAlerts();

        verify(notifRepo, never()).save(any());
    }

    // ---------- helpers ----------

    private Inventory inventory(String name, LocalDate expiry) {
        Inventory i = new Inventory(user, name, BigDecimal.ONE, Unit.PIECE, expiry);
        ReflectionTestUtils.setField(i, "id", UUID.randomUUID());
        return i;
    }
}