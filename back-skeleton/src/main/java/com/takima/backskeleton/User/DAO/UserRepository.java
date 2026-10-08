package com.takima.backskeleton.User.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.takima.backskeleton.User.models.User;
import com.takima.backskeleton.User.models.Role;


import java.util.Optional;
import java.util.UUID;
 
/**
 * DAO. Les requêtes dérivées de Spring Data sont paramétrées
 * (protection contre l'injection SQL). Pas de concaténation de SQL.
 * Les emails sont stockés en minuscules.
 */

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
 
    Optional<User> findByEmail(String email);
 
    boolean existsByEmail(String email);
 
    long countByRole(Role role);
}

