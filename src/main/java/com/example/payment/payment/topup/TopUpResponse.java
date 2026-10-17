package com.example.payment.payment.topup;

public record TopUpResponse(
        String topUpOrderId, String userId, String cardId, String walletId,
        String amount, String currency, String status, String failureReason,
        boolean walletUpdated, boolean ledgerUpdated) {
    public static TopUpResponse from(TopUpOrder order) {
        return new TopUpResponse(order.getTopUpOrderId(), order.getUserId(), order.getCardId(),
                order.getWalletId(), order.getAmount().toPlainString(), order.getCurrency(),
                order.getStatus().name(), order.getFailureReason(), order.isWalletUpdated(),
                order.isLedgerUpdated());
    }
}
