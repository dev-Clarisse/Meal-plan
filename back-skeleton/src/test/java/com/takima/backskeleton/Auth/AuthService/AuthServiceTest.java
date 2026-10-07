package com.takima.backskeleton.Auth.AuthService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.takima.backskeleton.Auth.JwtService;
import com.takima.backskeleton.Auth.LoginRequest;
import com.takima.backskeleton.Auth.TokenResponse;
import com.takima.backskeleton.User.DAO.UserRepository;
import com.takima.backskeleton.User.models.User;

/**
 * Test UNITAIRE : on teste AuthService seul, sans Spring, sans base de données.
 * Ses 3 dépendances sont remplacées par des "mocks" (doublures contrôlées par le test).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    // Mockito crée AuthService en lui passant les 3 mocks ci-dessus
    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Identifiants valides : renvoie un token Bearer avec sa durée de vie")
    void login_withValidCredentials_returnsToken() {
        // Arrange (préparer)
        LoginRequest request = new LoginRequest("clarisse@mail.com", "motdepasse-12345");
        User user = mock(User.class);
        when(userRepository.findByEmail("clarisse@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.generate(user)).thenReturn("jwt-factice");
        when(jwtService.ttlSeconds()).thenReturn(900L);

        // Act (agir)
        TokenResponse response = authService.login(request);

        // Assert (vérifier)
        assertThat(response.accessToken()).isEqualTo("jwt-factice");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
    }

    @Test
    @DisplayName("L'email est nettoyé (espaces, majuscules) avant l'authentification")
    void login_normalizesEmail() {
        LoginRequest request = new LoginRequest("  Clarisse@Mail.COM ", "motdepasse-12345");
        User user = mock(User.class);
        when(userRepository.findByEmail("clarisse@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.generate(user)).thenReturn("jwt-factice");
        when(jwtService.ttlSeconds()).thenReturn(900L);

        authService.login(request);

        // On "capture" ce qui a été envoyé à l'AuthenticationManager pour l'inspecter
        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("clarisse@mail.com");
    }

    @Test
    @DisplayName("Mauvais mot de passe : 401 et aucun token généré")
    void login_withBadCredentials_throwsUnauthorized() {
        LoginRequest request = new LoginRequest("clarisse@mail.com", "mauvais-mot-de-passe");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("raté"));

        ResponseStatusException ex =
            assertThrows(ResponseStatusException.class, () -> authService.login(request));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getReason()).isEqualTo("Identifiants invalides");
        // Comme l'authentification a échoué, on ne doit ni chercher l'utilisateur ni créer de JWT
        verifyNoInteractions(userRepository, jwtService);
    }

    @Test
    @DisplayName("Utilisateur introuvable après authentification : même 401, même message")
    void login_whenUserNotFound_throwsSameUnauthorized() {
        LoginRequest request = new LoginRequest("fantome@mail.com", "motdepasse-12345");
        when(userRepository.findByEmail("fantome@mail.com")).thenReturn(Optional.empty());

        ResponseStatusException ex =
            assertThrows(ResponseStatusException.class, () -> authService.login(request));

        // Même message que pour un mauvais mot de passe : on ne révèle pas si l'email existe
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getReason()).isEqualTo("Identifiants invalides");
        verifyNoInteractions(jwtService);
    }
}