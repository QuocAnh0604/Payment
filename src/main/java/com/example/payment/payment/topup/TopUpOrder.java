package com.example.payment.payment.topup;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "top_up_orders")
@Getter
@Setter
@NoArgsConstructor
public class TopUpOrder {
    @Id
    @Column(name = "top_up_order_id")
    private String topUpOrderId;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "card_id", nullable = false)
    private String cardId;
    @Column(name = "wallet_id", nullable = false)
    private String walletId;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopUpStatus status;
    @Column(name = "failure_reason")
    private String failureReason;
    @Column(name = "wallet_updated", nullable = false)
    private boolean walletUpdated;
    @Column(name = "ledger_updated", nullable = false)
    private boolean ledgerUpdated;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
