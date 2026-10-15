package com.example.payment.payment.service;

import com.example.payment.card.*;
import com.example.payment.payment.dto.ExecutePaymentRequest;
import com.example.payment.payment.entity.*;
import com.example.payment.payment.exception.BusinessException;
import com.example.payment.payment.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final CardRepository cardRepo;
    private final WalletRepository walletRepo;
    private final PaymentEventRepository eventRepo;
    private final PaymentOrderRepository orderRepo;
    private final LedgerService ledgerService;

    @Transactional
    public PaymentOrder execute(ExecutePaymentRequest req) {
        Optional<PaymentOrder> existing = orderRepo.findById(req.paymentOrderId());
        if (existing.isPresent()) return existing.get();
        BigDecimal amount = parseAmount(req.amount());
        if (req.sourceCardId().equals(req.destinationCardId()))
            throw BusinessException.badRequest("SAME_CARD", "Thẻ nguồn và thẻ đích phải khác nhau");
        Card source = cardRepo.findById(req.sourceCardId()).orElseThrow(() -> BusinessException.notFound("CARD_NOT_FOUND", "Không thấy thẻ nguồn"));
        Card dest = cardRepo.findById(req.destinationCardId()).orElseThrow(() -> BusinessException.notFound("CARD_NOT_FOUND", "Không thấy thẻ đích"));
        if (!source.getUserId().equals(req.userId()))
            throw BusinessException.badRequest("CARD_NOT_OWNED", "Thẻ nguồn không thuộc user này");
        if (!source.isUsable() || !dest.isUsable())
            throw BusinessException.badRequest("CARD_INACTIVE", "Thẻ bị khóa hoặc hết hạn");
        PaymentEvent event = eventRepo.findById(req.checkoutId()).orElseGet(() -> {
            PaymentEvent e = new PaymentEvent();
            e.setCheckoutId(req.checkoutId());
            e.setUserId(req.userId());
            return eventRepo.save(e);
        });
        PaymentOrder order = new PaymentOrder();
        order.setPaymentOrderId(req.paymentOrderId());
        order.setCheckoutId(event.getCheckoutId());
        order.setSourceCardId(source.getCardId());
        order.setDestinationCardId(dest.getCardId());
        order.setAmount(amount);
        order.setCurrency(req.currency());
        order.setStatus(PaymentOrderStatus.EXECUTING);
        orderRepo.save(order);
        String first = source.getCardId().compareTo(dest.getCardId()) < 0 ? source.getCardId() : dest.getCardId();
        String second = first.equals(source.getCardId()) ? dest.getCardId() : source.getCardId();
        Wallet w1 = lockWallet(first);
        Wallet w2 = lockWallet(second);
        Wallet from = w1.getCardId().equals(source.getCardId()) ? w1 : w2;
        Wallet to = from == w1 ? w2 : w1;
        if (!from.getCurrency().equals(req.currency()) || !to.getCurrency().equals(req.currency()))
            return fail(order, "CURRENCY_MISMATCH");
        if (!from.hasEnough(amount)) return fail(order, "INSUFFICIENT_BALANCE");
        from.debit(amount);
        to.credit(amount);
        order.setWalletUpdated(true);
        ledgerService.recordTransfer(order.getPaymentOrderId(), from.getCardId(), to.getCardId(), amount, req.currency());
        order.setLedgerUpdated(true);
        order.setStatus(PaymentOrderStatus.SUCCESS);
        event.setPaymentDone(true);
        return order;
    }

    @Transactional(readOnly = true)
    public PaymentOrder getOrder(String paymentOrderId) {
        return orderRepo.findById(paymentOrderId).orElseThrow(() -> BusinessException.notFound("ORDER_NOT_FOUND", "Không thấy payment order"));
    }

    private Wallet lockWallet(String cardId) {
        return walletRepo.findByCardIdForUpdate(cardId).orElseThrow(() -> BusinessException.notFound("WALLET_NOT_FOUND", "Thẻ " + cardId + " chưa có ví"));
    }

    private PaymentOrder fail(PaymentOrder order, String reason) {
        order.setStatus(PaymentOrderStatus.FAILED);
        order.setFailureReason(reason);
        return order;
    }

    private BigDecimal parseAmount(String raw) {
        try {
            BigDecimal v = new BigDecimal(raw);
            if (v.signum() <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException | NullPointerException ex) {
            throw BusinessException.badRequest("INVALID_AMOUNT", "amount phải là số dương");
        }
    }
}
