package com.example.payment.user;

public record UserResponse(
        String userId,
        String fullName,
        String email,
        String phone,
        String nationalId,
        String address,
        String status) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getFullName(), user.getEmail(),
                user.getPhone(), user.getNationalId(), user.getAddress(), user.getStatus().name());
    }
}
