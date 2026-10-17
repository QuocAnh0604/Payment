package com.example.payment.auth;

import com.example.payment.payment.entity.EntityStatus;
import com.example.payment.payment.exception.BusinessException;
import com.example.payment.user.User;
import com.example.payment.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final long REFRESH_TOKEN_TTL_SECONDS = 3600;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> BusinessException.badRequest("INVALID_CREDENTIALS", "Email hoặc password không đúng"));
        if (user.getStatus() != EntityStatus.ACTIVE || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw BusinessException.badRequest("INVALID_CREDENTIALS", "Email hoặc password không đúng");
        return issueTokens(user);
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.findByEmail(email).isPresent())
            throw BusinessException.badRequest("EMAIL_EXISTS", "Email đã được sử dụng");
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setFullName(request.fullName());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(EntityStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> BusinessException.badRequest("INVALID_REFRESH_TOKEN", "Refresh token không hợp lệ"));
        if (stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(LocalDateTime.now()))
            throw BusinessException.badRequest("INVALID_REFRESH_TOKEN", "Refresh token đã hết hạn hoặc bị thu hồi");
        User user = userRepository.findById(stored.getUserId())
                .filter(u -> u.getStatus() == EntityStatus.ACTIVE)
                .orElseThrow(() -> BusinessException.badRequest("INVALID_REFRESH_TOKEN", "User không hoạt động"));
        stored.setRevokedAt(LocalDateTime.now());
        return issueTokens(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken)).ifPresent(token -> {
            token.setRevokedAt(LocalDateTime.now());
        });
    }

    private AuthResponse issueTokens(User user) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawRefreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken token = new RefreshToken();
        token.setTokenId(UUID.randomUUID().toString());
        token.setUserId(user.getUserId());
        token.setTokenHash(hash(rawRefreshToken));
        token.setExpiresAt(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS));
        refreshTokenRepository.save(token);
        return new AuthResponse(jwtService.createAccessToken(user.getUserId(), user.getEmail()), rawRefreshToken,
                jwtService.accessTokenExpiresInSeconds(), REFRESH_TOKEN_TTL_SECONDS, user.getUserId());
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
