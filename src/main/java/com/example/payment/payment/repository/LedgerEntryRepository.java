package com.example.payment.payment.repository;

import com.example.payment.payment.entity.LedgerEntry;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByPaymentOrderId(String paymentOrderId);

    @Query("""
            select coalesce(sum(case when e.entryType = 'CREDIT' then e.amount
                                     else -e.amount end), 0)
            from LedgerEntry e where e.cardId = :cardId
            """)
    BigDecimal netAmountByCard(@Param("cardId") String cardId);
}
