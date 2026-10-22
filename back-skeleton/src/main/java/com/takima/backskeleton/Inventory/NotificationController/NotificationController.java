package com.takima.backskeleton.Inventory.NotificationController;

import com.takima.backskeleton.Inventory.DTO.NotificationResponse;
import com.takima.backskeleton.Inventory.NotificationService.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) { this.service = service; }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.listForUser(userId(jwt));
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("count", service.unreadCount(userId(jwt)));
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.markAsRead(userId(jwt), id);
    }

    @PostMapping("/read-all")
    public void markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        service.markAllAsRead(userId(jwt));
    }
}