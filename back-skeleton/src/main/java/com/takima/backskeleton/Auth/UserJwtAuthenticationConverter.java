package com.takima.backskeleton.Auth;

import com.takima.backskeleton.User.models.User;
import com.takima.backskeleton.User.DAO.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    public UserJwtAuthenticationConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID userId;
        try {
            userId = UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidBearerTokenException("Token invalide");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidBearerTokenException("Token invalide"));

        return new JwtAuthenticationToken(
            jwt,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
            user.getId().toString()
        );
    }
}
