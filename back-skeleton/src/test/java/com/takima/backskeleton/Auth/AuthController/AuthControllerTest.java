package com.takima.backskeleton.Auth.AuthController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.takima.backskeleton.Auth.AuthService.AuthService;
import com.takima.backskeleton.Auth.LoginRequest;
import com.takima.backskeleton.Auth.TokenResponse;

/**
 * Test de la couche WEB : on démarre seulement le contrôleur (@WebMvcTest) et on envoie
 * de vraies requêtes HTTP simulées avec MockMvc. AuthService est un mock.
 *
 * addFilters = false : on désactive Spring Security ici, car on teste le contrôleur
 * (JSON, validation, codes HTTP), pas la sécurité.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login valide : 200 et JSON {accessToken, tokenType, expiresIn}")
    void login_withValidBody_returns200AndToken() throws Exception {
        when(authService.login(any(LoginRequest.class)))
            .thenReturn(new TokenResponse("jwt-factice", "Bearer", 900));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "clarisse@mail.com", "password": "motdepasse-12345"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("jwt-factice"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    @DisplayName("Email vide : 400 et le service n'est jamais appelé")
    void login_withBlankEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "", "password": "motdepasse-12345"}
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Mot de passe trop long (> 72) : 400")
    void login_withTooLongPassword_returns400() throws Exception {
        String longPassword = "a".repeat(73);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"clarisse@mail.com\", \"password\": \"" + longPassword + "\"}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Corps de requête absent : 400")
    void login_withoutBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Le service refuse les identifiants : le contrôleur renvoie 401")
    void login_whenServiceRejects_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "clarisse@mail.com", "password": "mauvais-mot-de-passe"}
                    """))
            .andExpect(status().isUnauthorized());
    }
}