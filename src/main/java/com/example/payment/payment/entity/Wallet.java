package com.example.payment.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
public class Wallet {
    @Id
    @Column(name = "wallet_id")
    private String walletId;
    @Column(name = "card_id", nullable = false, unique = true)
    private String cardId;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;
    @Column(nullable = false, length = 3)
    private String currency;
    @Version
    private Long version;

    public void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public boolean hasEnough(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }
}
