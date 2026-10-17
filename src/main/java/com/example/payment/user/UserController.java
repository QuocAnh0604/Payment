package com.example.payment.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public UserResponse get(Authentication authentication) {
        return UserResponse.from(userService.get(authentication.getName()));
    }

    @PutMapping
    public UserResponse update(@Valid @RequestBody UserUpdateRequest request,
                               Authentication authentication) {
        return UserResponse.from(userService.update(authentication.getName(), request));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(Authentication authentication) {
        userService.delete(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
