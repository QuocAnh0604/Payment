package com.example.payment.otp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {
    Optional<OtpChallenge> findTopByUserIdAndPurposeAndReferenceIdAndUsedAtIsNullOrderByCreatedAtDesc(
            String userId, OtpPurpose purpose, String referenceId);
}
