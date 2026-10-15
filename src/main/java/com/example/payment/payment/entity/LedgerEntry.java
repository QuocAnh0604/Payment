package com.example.payment.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;
    @Column(name = "payment_order_id", nullable = false, updatable = false)
    private String paymentOrderId;
    @Column(name = "card_id", nullable = false, updatable = false)
    private String cardId;
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, updatable = false)
    private EntryType entryType;
    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, updatable = false, length = 3)
    private String currency;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public static LedgerEntry of(String orderId, String cardId, EntryType type, BigDecimal amount, String currency) {
        LedgerEntry e = new LedgerEntry();
        e.paymentOrderId = orderId;
        e.cardId = cardId;
        e.entryType = type;
        e.amount = amount;
        e.currency = currency;
        return e;
    }
}
