package com.example.payment.card;

import com.example.payment.payment.entity.EntityStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
public class Card {
    @Id
    @Column(name = "card_id")
    private String cardId;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(nullable = false)
    private String issuer;
    @Column(name = "card_token", nullable = false, unique = true)
    private String cardToken;
    @Column(nullable = false, length = 4)
    private String last4;
    @Column(name = "card_type", nullable = false)
    private String cardType;
    @Column(name = "issue_date")
    private LocalDate issueDate;
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
    @Enumerated(EnumType.STRING)
    private EntityStatus status = EntityStatus.ACTIVE;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isUsable() {
        return status == EntityStatus.ACTIVE && expiryDate.isAfter(LocalDate.now());
    }
}
