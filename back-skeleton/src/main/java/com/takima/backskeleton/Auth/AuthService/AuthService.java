package com.takima.backskeleton.Auth.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.takima.backskeleton.Auth.JwtService;
import com.takima.backskeleton.User.DAO.UserRepository;
import com.takima.backskeleton.User.models.User;
import com.takima.backskeleton.Auth.TokenResponse;
import com.takima.backskeleton.Auth.LoginRequest;

import java.util.Locale;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final com.takima.backskeleton.User.DAO.UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        try {
            // DaoAuthenticationProvider : compare le hash BCrypt, y compris pour un email
            // inconnu (atténue l'énumération par temps de réponse)
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException e) {
            log.warn("Échec d'authentification");
            throw invalidCredentials();
        }

        User user = userRepository.findByEmail(email).orElseThrow(AuthService::invalidCredentials);
        log.info("Connexion réussie id={}", user.getId());
        return new TokenResponse(jwtService.generate(user), "Bearer", jwtService.ttlSeconds());
    }

    // Même message quel que soit le motif (email inconnu / mot de passe faux)
    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
    }
}
