package com.takima.backskeleton.User.DTO;

import com.takima.backskeleton.User.models.Role;

import jakarta.validation.constraints.NotNull;
 
public record RoleUpdateRequest(@NotNull Role role) {
}

