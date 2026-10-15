package com.example.payment.payment.repository;

import com.example.payment.payment.entity.Wallet;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.cardId = :cardId")
    Optional<Wallet> findByCardIdForUpdate(@Param("cardId") String cardId);
}
