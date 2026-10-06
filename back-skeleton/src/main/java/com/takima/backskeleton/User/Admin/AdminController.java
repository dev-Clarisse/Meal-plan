package com.takima.backskeleton.User.Admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.takima.backskeleton.User.UserService.UserService;
import com.takima.backskeleton.User.DTO.UserResponse;
import com.takima.backskeleton.User.DTO.PageResponse;
import com.takima.backskeleton.User.DTO.RoleUpdateRequest;


import java.util.UUID;

/**
 * Réservé aux ADMIN. Double protection :
 *  1. SecurityConfig : requestMatchers("/api/admin/**").hasRole("ADMIN")
 *  2. @PreAuthorize (défense en profondeur si la config d'URL est modifiée par erreur)
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public PageResponse<UserResponse> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return userService.list(page, size);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable UUID id,
                                   @Valid @RequestBody RoleUpdateRequest request,
                                   Authentication authentication) {
        return userService.changeRole(UUID.fromString(authentication.getName()), id, request.role());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Authentication authentication) {
        userService.deleteAsAdmin(UUID.fromString(authentication.getName()), id);
    }
}
