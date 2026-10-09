package com.takima.backskeleton.Inventory.InventoryService;

import com.takima.backskeleton.Inventory.DAO.InventoryRepository;
import com.takima.backskeleton.Inventory.InventoryService.InventoryService;
import com.takima.backskeleton.Inventory.DTO.InventoryRequest;
import com.takima.backskeleton.Inventory.DTO.InventoryResponse;
import com.takima.backskeleton.Inventory.models.Inventory;
import com.takima.backskeleton.Inventory.models.Unit;
import com.takima.backskeleton.User.DAO.UserRepository;
import com.takima.backskeleton.User.models.User;
import com.takima.backskeleton.config.InventoryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock InventoryRepository repo;
    @Mock UserRepository userRepo;

    InventoryService service;

    UUID userId = UUID.randomUUID();
    String userEmail = "user@test.com";
    User user;

    @BeforeEach
    void setUp() {
        InventoryProperties props = new InventoryProperties(3);
        service = new InventoryService(repo, userRepo, props);
        user = new User(userEmail, "hash", Instant.now());
        ReflectionTestUtils.setField(user, "id", userId);
    }

    // ---------- listForUser ----------

    @Test
    void listForUser_shouldReturnItemsWithStatus() {
        LocalDate today = LocalDate.now();
        Inventory fresh = inventory("Lait", LocalDate.now().plusDays(10));   // OK
        Inventory soon = inventory("Yaourt", today.plusDays(2));              // EXPIRING_SOON
        Inventory expired = inventory("Fromage", today.minusDays(1));         // EXPIRED
        Inventory noDate = inventory("Riz", null);                            // NO_DATE

        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(repo.findAllByUserIdOrderByExpiryDateAsc(userId))
            .thenReturn(List.of(expired, soon, fresh, noDate));

        List<InventoryResponse> result = service.listForUser(userEmail);

        assertThat(result).hasSize(4);
        assertThat(result.get(0).status()).isEqualTo(InventoryResponse.ExpiryStatus.EXPIRED);
        assertThat(result.get(1).status()).isEqualTo(InventoryResponse.ExpiryStatus.EXPIRING_SOON);
        assertThat(result.get(2).status()).isEqualTo(InventoryResponse.ExpiryStatus.OK);
        assertThat(result.get(3).status()).isEqualTo(InventoryResponse.ExpiryStatus.NO_DATE);
    }

    @Test
    void listForUser_shouldThrowIfUserUnknown() {
        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listForUser(userEmail))
            .isInstanceOf(AccessDeniedException.class);
    }

    // ---------- create ----------

    @Test
    void create_shouldSaveAndReturn() {
        InventoryRequest req = new InventoryRequest(
            "Lait", new BigDecimal("1.500"), Unit.LITER, LocalDate.now().plusDays(5)
        );

        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(repo.save(any(Inventory.class))).thenAnswer(inv -> {
            Inventory i = inv.getArgument(0);
            ReflectionTestUtils.setField(i, "id", UUID.randomUUID());
            return i;
        });

        InventoryResponse response = service.create(userEmail, req);

        assertThat(response.name()).isEqualTo("Lait");
        assertThat(response.quantity()).isEqualByComparingTo("1.500");
        assertThat(response.unit()).isEqualTo(Unit.LITER);
        verify(repo).save(any(Inventory.class));
    }

    // ---------- update ----------

    @Test
    void update_shouldModifyExistingItem() {
        UUID invId = UUID.randomUUID();
        Inventory existing = new Inventory(user, "Lait", new BigDecimal("1"), Unit.LITER, LocalDate.now());
        ReflectionTestUtils.setField(existing, "id", invId);

        InventoryRequest req = new InventoryRequest(
            "Lait entier", new BigDecimal("2"), Unit.LITER, LocalDate.now().plusDays(3)
        );

        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(repo.findByIdAndUserId(invId, userId)).thenReturn(Optional.of(existing));
        when(repo.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryResponse response = service.update(userEmail, invId, req);

        assertThat(response.name()).isEqualTo("Lait entier");
        assertThat(response.quantity()).isEqualByComparingTo("2");
    }

    @Test
    void update_shouldThrowIfNotOwned() {
        UUID invId = UUID.randomUUID();
        InventoryRequest req = new InventoryRequest("X", BigDecimal.ONE, Unit.PIECE, null);

        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(repo.findByIdAndUserId(invId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(userEmail, invId, req))
            .isInstanceOf(AccessDeniedException.class);
        verify(repo, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void delete_shouldRemoveOwnedItem() {
        UUID invId = UUID.randomUUID();
        Inventory existing = new Inventory(user, "Lait", BigDecimal.ONE, Unit.LITER, null);
        ReflectionTestUtils.setField(existing, "id", invId);

        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(repo.findByIdAndUserId(invId, userId)).thenReturn(Optional.of(existing));

        service.delete(userEmail, invId);

        verify(repo).delete(existing);
    }

    @Test
    void delete_shouldThrowIfNotOwned() {
        UUID invId = UUID.randomUUID();

        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(repo.findByIdAndUserId(invId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(userEmail, invId))
            .isInstanceOf(AccessDeniedException.class);
        verify(repo, never()).delete(any());
    }

    // ---------- helpers ----------

    private Inventory inventory(String name, LocalDate expiry) {
        Inventory i = new Inventory(user, name, BigDecimal.ONE, Unit.PIECE, expiry);
        ReflectionTestUtils.setField(i, "id", UUID.randomUUID());
        return i;
    }
}