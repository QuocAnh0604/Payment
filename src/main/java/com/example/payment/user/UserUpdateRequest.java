package com.example.payment.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        String phone,
        String nationalId,
        String address,
        @Size(min = 8, max = 100) String password) {
}
