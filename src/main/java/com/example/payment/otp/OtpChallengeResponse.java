package com.example.payment.otp;

import java.time.LocalDateTime;
import java.util.UUID;

public record OtpChallengeResponse(
        UUID otpId,
        OtpPurpose purpose,
        String referenceId,
        OtpChannel channel,
        String recipientHint,
        LocalDateTime expiresAt,
        int attemptsRemaining,
        boolean verified) {
    public static OtpChallengeResponse from(OtpChallenge challenge) {
        return new OtpChallengeResponse(challenge.getOtpId(), challenge.getPurpose(),
                challenge.getReferenceId(), challenge.getChannel(), challenge.getRecipientHint(),
                challenge.getExpiresAt(), Math.max(0, challenge.getMaxAttempts() - challenge.getAttemptCount()),
                challenge.getUsedAt() != null);
    }
}
