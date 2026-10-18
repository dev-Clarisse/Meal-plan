package com.takima.backskeleton.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    // Le "subject" du jeton JWT est l'UUID de l'utilisateur
    public UUID idOf(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new org.springframework.security.access.AccessDeniedException("Invalid token subject");
        }
    }
}