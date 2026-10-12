package com.takima.backskeleton.User.DTO;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
 
public record UserCreateRequest(
 
    @NotBlank
    @Email
    @Size(max = 254)
    String email,
 
    @NotBlank
    @Size(min = 12, max = 72, message = "Le mot de passe doit contenir entre 12 et 72 caractères")
    String password,
 
    @AssertTrue(message = "Le consentement au traitement des données est obligatoire")
    boolean consent
) {
    @Override
    public String toString() {
        return "UserCreateRequest[email=***, password=***]";
    }
}

