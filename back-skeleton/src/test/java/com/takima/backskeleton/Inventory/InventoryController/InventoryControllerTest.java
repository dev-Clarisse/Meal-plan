package com.takima.backskeleton.Inventory.InventoryController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takima.backskeleton.Inventory.DTO.InventoryRequest;
import com.takima.backskeleton.Inventory.DTO.InventoryResponse;
import com.takima.backskeleton.Inventory.InventoryService.InventoryService;
import com.takima.backskeleton.Inventory.models.Unit;
import com.takima.backskeleton.User.DAO.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean InventoryService service;
    @MockBean UserRepository userRepository;   // requis par UserJwtAuthenticationConverter

    private static final String EMAIL = "user@test.com";

    private InventoryResponse sampleResponse() {
        return new InventoryResponse(
            UUID.randomUUID(), "Lait", new BigDecimal("1.5"), Unit.LITER,
            LocalDate.now().plusDays(5), InventoryResponse.ExpiryStatus.OK,
            Instant.now(), Instant.now()
        );
    }

    @Test
    void list_shouldReturnItems() throws Exception {
        when(service.listForUser(EMAIL)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/inventory")
                .with(jwt().jwt(j -> j.subject(EMAIL))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Lait"))
            .andExpect(jsonPath("$[0].unit").value("LITER"))
            .andExpect(jsonPath("$[0].status").value("OK"));
    }

    @Test
    void list_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/inventory"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        InventoryRequest req = new InventoryRequest(
            "Yaourt", new BigDecimal("2"), Unit.PIECE, LocalDate.now().plusDays(3)
        );
        InventoryResponse resp = new InventoryResponse(
            UUID.randomUUID(), "Yaourt", new BigDecimal("2"), Unit.PIECE,
            LocalDate.now().plusDays(3), InventoryResponse.ExpiryStatus.EXPIRING_SOON,
            Instant.now(), Instant.now()
        );
        when(service.create(eq(EMAIL), any(InventoryRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/inventory")
                .with(jwt().jwt(j -> j.subject(EMAIL)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Yaourt"))
            .andExpect(jsonPath("$.status").value("EXPIRING_SOON"));
    }

    @Test
    void create_withInvalidBody_shouldReturn400() throws Exception {
        // name vide → @NotBlank déclenche
        InventoryRequest invalid = new InventoryRequest("", new BigDecimal("1"), Unit.PIECE, null);

        mockMvc.perform(post("/api/inventory")
                .with(jwt().jwt(j -> j.subject(EMAIL)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnItem() throws Exception {
        UUID id = UUID.randomUUID();
        InventoryRequest req = new InventoryRequest(
            "Lait entier", new BigDecimal("2"), Unit.LITER, LocalDate.now().plusDays(3)
        );
        when(service.update(eq(EMAIL), eq(id), any(InventoryRequest.class)))
            .thenReturn(sampleResponse());

        mockMvc.perform(put("/api/inventory/{id}", id)
                .with(jwt().jwt(j -> j.subject(EMAIL)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/inventory/{id}", id)
                .with(jwt().jwt(j -> j.subject(EMAIL))))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn403WhenNotOwned() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new AccessDeniedException("not yours"))
            .when(service).delete(EMAIL, id);

        mockMvc.perform(delete("/api/inventory/{id}", id)
                .with(jwt().jwt(j -> j.subject(EMAIL))))
            .andExpect(status().isForbidden());
    }
}