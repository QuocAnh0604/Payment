package com.example.payment.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_events")
@Getter
@Setter
@NoArgsConstructor
public class PaymentEvent {
    @Id
    @Column(name = "checkout_id")
    private String checkoutId;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "is_payment_done", nullable = false)
    private boolean paymentDone;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
