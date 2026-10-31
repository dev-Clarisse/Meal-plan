package com.takima.backskeleton.Inventory.NotificationService;

import com.takima.backskeleton.Inventory.DAO.NotificationRepository;
import com.takima.backskeleton.Inventory.DTO.NotificationResponse;
import com.takima.backskeleton.Inventory.models.Notification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForUser(UUID userId) {
        return repo.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(n -> new NotificationResponse(
                n.getId(), n.getInventory().getId(), n.getInventory().getName(),
                n.getType(), n.getMessage(), n.isRead(), n.getCreatedAt()
            )).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return repo.countByUserIdAndReadIsFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification n = repo.findById(notificationId)
            .filter(notif -> notif.getUser().getId().equals(userId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        n.setRead(true);
        repo.save(n);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        repo.findAllByUserIdOrderByCreatedAtDesc(userId).forEach(n -> {
            if (!n.isRead()) { n.setRead(true); repo.save(n); }
        });
    }
}