package com.example.payment.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtpCreateRequest(
        @NotNull OtpPurpose purpose,
        @NotBlank String referenceId,
        @NotNull OtpChannel channel) {
}
