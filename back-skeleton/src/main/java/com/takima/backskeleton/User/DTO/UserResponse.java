package com.takima.backskeleton.User.DTO;


import com.takima.backskeleton.User.models.Role;
import com.takima.backskeleton.User.models.User;

import java.time.Instant;
import java.util.UUID;
 
/**
 * DTO de sortie : n'expose JAMAIS le hash du mot de passe.
 * Sert aussi pour le droit d'accès / portabilité (RGPD art. 15 et 20).
 */
public record UserResponse(
    UUID id,
    String email,
    Role role,
    Instant consentGivenAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getConsentGivenAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

