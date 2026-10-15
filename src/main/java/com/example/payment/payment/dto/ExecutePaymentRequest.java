package com.example.payment.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExecutePaymentRequest(
        @NotBlank String checkoutId, @NotBlank String paymentOrderId, @NotBlank String userId,
        @NotBlank String sourceCardId, @NotBlank String destinationCardId, @NotBlank String amount,
        @NotBlank @Size(min = 3, max = 3) String currency) {
}
