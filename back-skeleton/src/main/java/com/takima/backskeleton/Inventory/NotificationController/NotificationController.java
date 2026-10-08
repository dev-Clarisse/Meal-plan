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

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.listForUser(jwt.getSubject());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("count", service.unreadCount(jwt.getSubject()));
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.markAsRead(jwt.getSubject(), id);
    }

    @PostMapping("/read-all")
    public void markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        service.markAllAsRead(jwt.getSubject());
    }
}