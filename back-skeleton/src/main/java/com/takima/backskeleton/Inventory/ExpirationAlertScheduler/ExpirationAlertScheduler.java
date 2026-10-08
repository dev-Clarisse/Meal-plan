package com.takima.backskeleton.Inventory.ExpirationAlertScheduler;

import com.takima.backskeleton.Inventory.DAO.InventoryRepository;
import com.takima.backskeleton.Inventory.DAO.NotificationRepository;
import com.takima.backskeleton.Inventory.models.Inventory;
import com.takima.backskeleton.Inventory.models.Notification;
import com.takima.backskeleton.Inventory.models.NotificationType;
import com.takima.backskeleton.config.InventoryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class ExpirationAlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpirationAlertScheduler.class);

    private final InventoryRepository inventoryRepo;
    private final NotificationRepository notifRepo;
    private final InventoryProperties props;

    public ExpirationAlertScheduler(InventoryRepository inventoryRepo,
                                    NotificationRepository notifRepo,
                                    InventoryProperties props) {
        this.inventoryRepo = inventoryRepo;
        this.notifRepo = notifRepo;
        this.props = props;
    }

    /** Tous les matins à 7h00 (heure serveur). */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void runDailyAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(props.expiringSoonDays());

        List<Inventory> candidates = inventoryRepo.findAllByExpiryDateLessThanEqual(threshold);
        log.info("Daily expiration alert scan: {} candidate(s)", candidates.size());

        for (Inventory inv : candidates) {
            LocalDate expiry = inv.getExpiryDate();
            if (expiry == null) continue;

            NotificationType type = expiry.isBefore(today)
                ? NotificationType.EXPIRED
                : NotificationType.EXPIRING_SOON;

            // Anti-doublon : contrainte unique (inventory_id, type)
            if (notifRepo.existsByInventoryIdAndType(inv.getId(), type)) continue;

            String msg = type == NotificationType.EXPIRED
                ? "Le produit « %s » a expiré le %s".formatted(inv.getName(), expiry)
                : "Le produit « %s » expire le %s".formatted(inv.getName(), expiry);

            notifRepo.save(new Notification(inv.getUser(), inv, type, msg));
            log.info("Created {} notification for inventory {}", type, inv.getId());
        }
    }
}