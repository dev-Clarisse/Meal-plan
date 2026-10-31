package com.takima.backskeleton.Inventory;

import com.takima.backskeleton.Inventory.DAO.InventoryRepository;
import com.takima.backskeleton.Inventory.DAO.NotificationRepository;
import com.takima.backskeleton.Inventory.InventoryService.InventoryService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
// Base H2 dédiée : le seed de démo ne doit pas se retrouver dans celle des autres tests
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:seedtest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        "spring.flyway.locations=classpath:db/migration,classpath:db/seed"
})
@Transactional
class InventorySeedIntegrationTest {

    // Données insérées par db/seed/R__demo_users_inventory.sql
    static final UUID ALICE = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    static final UUID MILK_WITH_NOTIFICATION = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11");

    @Autowired InventoryRepository inventoryRepo;
    @Autowired NotificationRepository notificationRepo;
    @Autowired InventoryService inventoryService;
    @Autowired EntityManager em;

    @Test
    void seededRowsUseKnownEnumValues() {
        assertThat(inventoryRepo.findAll()).hasSize(3);
        assertThat(notificationRepo.findAll()).hasSize(2);
    }

    @Test
    void deletingItemAlsoDeletesItsNotifications() {
        inventoryService.delete(ALICE, MILK_WITH_NOTIFICATION);
        em.flush();

        assertThat(inventoryRepo.findById(MILK_WITH_NOTIFICATION)).isEmpty();
        assertThat(notificationRepo.findAllByUserIdOrderByCreatedAtDesc(ALICE)).isEmpty();
    }
}
