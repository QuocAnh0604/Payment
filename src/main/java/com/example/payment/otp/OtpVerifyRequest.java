package com.example.payment.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(
        @NotBlank @Pattern(regexp = "\\d{6}") String code) {
}
