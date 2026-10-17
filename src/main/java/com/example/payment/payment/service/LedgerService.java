package com.example.payment.payment.service;

import com.example.payment.payment.entity.*;
import com.example.payment.payment.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerEntryRepository ledgerRepo;

    public void recordTransfer(String orderId, String fromCard, String toCard, BigDecimal amount, String currency) {
        ledgerRepo.save(LedgerEntry.of(orderId, fromCard, EntryType.DEBIT, amount, currency));
        ledgerRepo.save(LedgerEntry.of(orderId, toCard, EntryType.CREDIT, amount, currency));
    }

    public void recordTopUp(String topUpOrderId, String cardId, BigDecimal amount, String currency) {
        ledgerRepo.save(LedgerEntry.ofTopUp(topUpOrderId, cardId, amount, currency));
    }
}
