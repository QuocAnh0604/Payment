package com.example.payment.payment.topup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TopUpRequest(
        @NotBlank String topUpOrderId,
        @NotBlank String cardId,
        @NotBlank String amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull UUID otpId) {
}
