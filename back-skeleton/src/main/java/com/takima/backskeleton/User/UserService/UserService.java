package com.takima.backskeleton.User.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.takima.backskeleton.Inventory.DAO.InventoryRepository;
import com.takima.backskeleton.Inventory.DAO.NotificationRepository;
import com.takima.backskeleton.User.DAO.UserRepository;
import com.takima.backskeleton.User.DTO.UserResponse;
import com.takima.backskeleton.User.DTO.UserCreateRequest;
import com.takima.backskeleton.User.models.User;
import com.takima.backskeleton.User.DTO.UserUpdateRequest;
import com.takima.backskeleton.User.models.Role;
import com.takima.backskeleton.User.DTO.PageResponse;



 
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
 
@Service
public class UserService {
 
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final int MAX_PAGE_SIZE = 100;
 
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, InventoryRepository inventoryRepository,
                       NotificationRepository notificationRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }
 
    @Transactional
    public UserResponse register(UserCreateRequest request) {
        String email = normalize(request.email());
 
        if (userRepository.existsByEmail(email)) {
            throw conflict();
        }
 
        User user = new User(email, passwordEncoder.encode(request.password()), Instant.now());
        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw conflict();
        }
 
        log.info("Utilisateur créé id={}", user.getId());
        return UserResponse.from(user);
    }
 
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return UserResponse.from(findOrThrow(id));
    }
 
    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request) {
        User user = findOrThrow(id);
 
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mot de passe actuel incorrect");
        }
 
        if (request.email() != null) {
            String newEmail = normalize(request.email());
            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw conflict();
                }
                user.setEmail(newEmail);
            }
        }
 
        if (request.newPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
 
        try {
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw conflict();
        }
 
        log.info("Utilisateur modifié id={}", user.getId());
        return UserResponse.from(user);
    }
 
    @Transactional
    public void delete(UUID id) {
        User user = findOrThrow(id);
 
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Impossible de supprimer le dernier administrateur");
        }
 
        // Pas de ON DELETE CASCADE sur inventory/notifications : on nettoie avant l'utilisateur.
        notificationRepository.deleteByUserId(id);
        inventoryRepository.deleteByUserId(id);
        userRepository.delete(user);
        log.info("Utilisateur supprimé id={}", id);
    }
 
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
 
        Page<User> result = userRepository.findAll(
            PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResponse.from(result.map(UserResponse::from));
    }
 
    @Transactional
    public UserResponse changeRole(UUID actorId, UUID targetId, Role newRole) {
        if (actorId.equals(targetId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Vous ne pouvez pas modifier votre propre rôle");
        }
 
        User user = findOrThrow(targetId);
        user.setRole(newRole);
        userRepository.flush();
 
        log.info("AUDIT rôle modifié admin={} cible={} nouveauRole={}", actorId, targetId, newRole);
        return UserResponse.from(user);
    }
 
    @Transactional
    public void deleteAsAdmin(UUID actorId, UUID targetId) {
        delete(targetId);
        log.info("AUDIT suppression admin={} cible={}", actorId, targetId);
    }
 
    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }
 
    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
 
    private static ResponseStatusException conflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Opération impossible avec ces informations");
    }
}

