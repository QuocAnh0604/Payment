package com.example.payment.payment.dto;

import com.example.payment.payment.entity.LedgerEntry;

public record LedgerEntryResponse(Long entryId, String paymentOrderId, String cardId, String entryType, String amount,
                                  String currency) {
    public static LedgerEntryResponse from(LedgerEntry e) {
        return new LedgerEntryResponse(e.getEntryId(), e.getPaymentOrderId(), e.getCardId(), e.getEntryType().name(), e.getAmount().toPlainString(), e.getCurrency());
    }
}
