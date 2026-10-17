package com.example.payment.auth;

public record AuthResponse(String accessToken, String refreshToken, long accessTokenExpiresInSeconds,
                           long refreshTokenExpiresInSeconds, String userId) {
}
