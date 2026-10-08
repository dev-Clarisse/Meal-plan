package com.takima.backskeleton.Inventory.NotificationService;

import com.takima.backskeleton.Inventory.DAO.NotificationRepository;
import com.takima.backskeleton.Inventory.DTO.NotificationResponse;
import com.takima.backskeleton.Inventory.models.Notification;
import com.takima.backskeleton.User.DAO.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository repo;
    private final UserRepository userRepo;

    public NotificationService(NotificationRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForUser(String email) {
        UUID userId = currentUserId(email);
        return repo.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(n -> new NotificationResponse(
                n.getId(), n.getInventory().getId(), n.getInventory().getName(),
                n.getType(), n.getMessage(), n.isRead(), n.getCreatedAt()
            )).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        return repo.countByUserIdAndReadIsFalse(currentUserId(email));
    }

    @Transactional
    public void markAsRead(String email, UUID notificationId) {
        UUID userId = currentUserId(email);
        Notification n = repo.findById(notificationId)
            .orElseThrow(() -> new AccessDeniedException("Notification not found"));
        if (!n.getUser().getId().equals(userId))
            throw new AccessDeniedException("Not yours");
        n.setRead(true);
        repo.save(n);
    }

    @Transactional
    public void markAllAsRead(String email) {
        UUID userId = currentUserId(email);
        repo.findAllByUserIdOrderByCreatedAtDesc(userId).forEach(n -> {
            if (!n.isRead()) { n.setRead(true); repo.save(n); }
        });
    }

    private UUID currentUserId(String email) {
        return userRepo.findByEmail(email)
            .orElseThrow(() -> new AccessDeniedException("Unknown user"))
            .getId();
    }
}