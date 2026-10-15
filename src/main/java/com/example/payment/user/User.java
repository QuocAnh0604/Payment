package com.example.payment.user;

import com.example.payment.payment.entity.EntityStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "user_id")
    private String userId;
    @Column(name = "full_name", nullable = false)
    private String fullName;
    @Column(nullable = false, unique = true)
    private String email;
    private String phone;
    @Column(name = "national_id")
    private String nationalId;
    private String address;
    @Enumerated(EnumType.STRING)
    private EntityStatus status = EntityStatus.ACTIVE;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
