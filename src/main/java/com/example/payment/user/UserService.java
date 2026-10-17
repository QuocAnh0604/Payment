package com.example.payment.user;

import com.example.payment.payment.entity.EntityStatus;
import com.example.payment.payment.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User get(String authenticatedUserId) {
        return findUser(authenticatedUserId);
    }

    @Transactional
    public User update(String authenticatedUserId, UserUpdateRequest request) {
        User user = findUser(authenticatedUserId);
        userRepository.findByEmail(request.email().toLowerCase()).ifPresent(existing -> {
            if (!existing.getUserId().equals(user.getUserId()))
                throw BusinessException.badRequest("EMAIL_EXISTS", "Email đã được sử dụng");
        });
        user.setFullName(request.fullName());
        user.setEmail(request.email().toLowerCase());
        user.setPhone(request.phone());
        user.setNationalId(request.nationalId());
        user.setAddress(request.address());
        if (request.password() != null && !request.password().isBlank())
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        return user;
    }

    @Transactional
    public void delete(String authenticatedUserId) {
        User user = findUser(authenticatedUserId);
        user.setStatus(EntityStatus.BLOCKED);
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "Không tìm thấy user"));
    }
}
