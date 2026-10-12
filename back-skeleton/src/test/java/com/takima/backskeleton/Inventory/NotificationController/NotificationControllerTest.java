package com.takima.backskeleton.Inventory.NotificationController;

import com.takima.backskeleton.Inventory.DTO.NotificationResponse;
import com.takima.backskeleton.Inventory.NotificationService.NotificationService;
import com.takima.backskeleton.Inventory.models.NotificationType;
import com.takima.backskeleton.User.DAO.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean NotificationService service;
    @MockBean UserRepository userRepository;   // requis par UserJwtAuthenticationConverter

    private static final String EMAIL = "user@test.com";

    @Test
    void list_shouldReturnNotifications() throws Exception {
        NotificationResponse resp = new NotificationResponse(
            UUID.randomUUID(), UUID.randomUUID(), "Lait",
            NotificationType.EXPIRING_SOON, "Le produit Lait expire bientôt",
            false, Instant.now()
        );
        when(service.listForUser(EMAIL)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/notifications")
                .with(jwt().jwt(j -> j.subject(EMAIL))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].inventoryName").value("Lait"))
            .andExpect(jsonPath("$[0].type").value("EXPIRING_SOON"))
            .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void unreadCount_shouldReturnCount() throws Exception {
        when(service.unreadCount(EMAIL)).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count")
                .with(jwt().jwt(j -> j.subject(EMAIL))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void markAsRead_shouldReturn200() throws Exception {
        UUID notifId = UUID.randomUUID();

        mockMvc.perform(post("/api/notifications/{id}/read", notifId)
                .with(jwt().jwt(j -> j.subject(EMAIL))))
            .andExpect(status().isOk());
    }

    @Test
    void markAllAsRead_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/notifications/read-all")
                .with(jwt().jwt(j -> j.subject(EMAIL))))
            .andExpect(status().isOk());
    }

    @Test
    void list_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isUnauthorized());
    }
}