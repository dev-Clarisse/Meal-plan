package com.takima.backskeleton.User.models;


import jakarta.persistence.*;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
 
/**
 * Entité utilisateur.
 * - UUID : identifiant non devinable (évite l'énumération / IDOR)
 * - Contrainte d'unicité sur l'email (garantie par la base)
 * - Mot de passe stocké uniquement sous forme de hash
 * - Rôle USER / ADMIN : jamais fourni par le client à l'inscription
 * - @Version : verrouillage optimiste
 * - Champs RGPD : date de consentement, dates de création / modification
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email")
)
public class User {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    @Column(name = "email", nullable = false, length = 254)
    private String email;
 
    // Hash BCrypt = 60 caractères
    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;
 
    // Preuve du consentement (RGPD art. 7)
    @Column(name = "consent_given_at", nullable = false, updatable = false)
    private Instant consentGivenAt;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
 
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
 
    @Version
    private Long version;
 
    protected User() {
        // requis par JPA
    }
 
    public User(String email, String passwordHash, Instant consentGivenAt) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.consentGivenAt = consentGivenAt;
    }
 
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        normalizeEmail();
    }
 
    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
        normalizeEmail();
    }
 
    private void normalizeEmail() {
        if (email != null) {
            this.email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
 
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Instant getConsentGivenAt() { return consentGivenAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
 
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && Objects.equals(id, other.id);
    }
 
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
 
    // Jamais de mot de passe ni d'email dans toString (fuite via les logs)
    @Override
    public String toString() {
        return "User{id=" + id + "}";
    }
}

