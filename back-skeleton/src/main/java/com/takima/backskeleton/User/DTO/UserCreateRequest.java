package com.takima.backskeleton.User.DTO;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
 
/** DTO d'entrée pour l'inscription. L'id n'est jamais fourni par le client. */
public record UserCreateRequest(
 
    @NotBlank
    @Email
    @Size(max = 254)
    String email,
 
    // 12 caractères minimum (recommandation CNIL/ANSSI) ; 72 = limite BCrypt
    @NotBlank
    @Size(min = 12, max = 72, message = "Le mot de passe doit contenir entre 12 et 72 caractères")
    String password,
 
    // Consentement explicite obligatoire (RGPD art. 6 et 7)
    @AssertTrue(message = "Le consentement au traitement des données est obligatoire")
    boolean consent
) {
    // Évite de divulguer le mot de passe dans les logs
    @Override
    public String toString() {
        return "UserCreateRequest[email=***, password=***]";
    }
}

