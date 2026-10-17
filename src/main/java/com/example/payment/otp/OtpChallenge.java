package com.example.payment.otp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_challenges")
@Getter
@Setter
@NoArgsConstructor
public class OtpChallenge {
    @Id
    @Column(name = "otp_id")
    private UUID otpId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpPurpose purpose;

    @Column(name = "reference_id", nullable = false, length = 64)
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OtpChannel channel;

    @Column(name = "recipient_hint")
    private String recipientHint;

    @Column(name = "otp_hash", nullable = false, length = 128)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
