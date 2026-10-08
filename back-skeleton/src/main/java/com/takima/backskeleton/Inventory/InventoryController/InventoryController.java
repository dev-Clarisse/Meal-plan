package com.takima.backskeleton.Inventory.InventoryController;

import com.takima.backskeleton.Inventory.DTO.InventoryRequest;
import com.takima.backskeleton.Inventory.DTO.InventoryResponse;
import com.takima.backskeleton.Inventory.InventoryService.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) { this.service = service; }

    @GetMapping
    public List<InventoryResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.listForUser(jwt.getSubject());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse create(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody InventoryRequest req) {
        return service.create(jwt.getSubject(), req);
    }

    @PutMapping("/{id}")
    public InventoryResponse update(@AuthenticationPrincipal Jwt jwt,
                                    @PathVariable UUID id,
                                    @Valid @RequestBody InventoryRequest req) {
        return service.update(jwt.getSubject(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.delete(jwt.getSubject(), id);
    }
}