package com.example.payment.payment.dto;

import com.example.payment.payment.entity.PaymentOrder;

public record PaymentOrderResponse(String paymentOrderId, String checkoutId, String sourceCardId,
                                   String destinationCardId, String amount, String currency, String status,
                                   String failureReason, boolean walletUpdated, boolean ledgerUpdated) {
    public static PaymentOrderResponse from(PaymentOrder o) {
        return new PaymentOrderResponse(o.getPaymentOrderId(), o.getCheckoutId(), o.getSourceCardId(),
                o.getDestinationCardId(), o.getAmount().toPlainString(), o.getCurrency(), o.getStatus().name(),
                o.getFailureReason(), o.isWalletUpdated(), o.isLedgerUpdated());
    }
}
