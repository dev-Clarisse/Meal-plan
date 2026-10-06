package com.takima.backskeleton.User.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
 
/** DTO de modification (droit de rectification, RGPD art. 16). */
public record UserUpdateRequest(
 
    @NotBlank
    String currentPassword,
 
    @Email
    @Size(max = 254)
    String email,
 
    @Size(min = 12, max = 72, message = "Le mot de passe doit contenir entre 12 et 72 caractères")
    String newPassword
) {
    @Override
    public String toString() {
        return "UserUpdateRequest[***]";
    }
}

