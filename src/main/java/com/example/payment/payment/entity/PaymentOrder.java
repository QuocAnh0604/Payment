package com.example.payment.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_orders")
@Getter
@Setter
@NoArgsConstructor
public class PaymentOrder {
    @Id
    @Column(name = "payment_order_id")
    private String paymentOrderId;
    @Column(name = "checkout_id", nullable = false)
    private String checkoutId;
    @Column(name = "source_card_id", nullable = false)
    private String sourceCardId;
    @Column(name = "destination_card_id", nullable = false)
    private String destinationCardId;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_order_status", nullable = false)
    private PaymentOrderStatus status;
    @Column(name = "failure_reason")
    private String failureReason;
    @Column(name = "ledger_updated", nullable = false)
    private boolean ledgerUpdated;
    @Column(name = "wallet_updated", nullable = false)
    private boolean walletUpdated;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
