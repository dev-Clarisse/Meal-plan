package com.takima.backskeleton.Auth;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
