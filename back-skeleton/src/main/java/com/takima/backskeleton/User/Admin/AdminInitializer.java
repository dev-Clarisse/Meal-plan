package com.takima.backskeleton.User.Admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.takima.backskeleton.User.DAO.UserRepository;
import com.takima.backskeleton.User.models.User;
import com.takima.backskeleton.User.models.Role;

import java.time.Instant;
import java.util.Locale;

/**
 * Crée le premier administrateur au démarrage si aucun n'existe et si
 * app.admin.email / app.admin.password sont fournis (variables d'environnement).
 * Retirez ces variables une fois l'admin créé.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.email:}") String adminEmail,
                            @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }
        if (userRepository.countByRole(Role.ADMIN) > 0) {
            return;
        }
        if (adminPassword.length() < 12) {
            throw new IllegalStateException("app.admin.password doit contenir au moins 12 caractères");
        }

        User admin = new User(
            adminEmail.trim().toLowerCase(Locale.ROOT),
            passwordEncoder.encode(adminPassword),
            Instant.now()
        );
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Premier administrateur créé id={}", admin.getId());
    }
}
