package com.example.payment.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    java.util.Optional<User> findByEmail(String email);
}
