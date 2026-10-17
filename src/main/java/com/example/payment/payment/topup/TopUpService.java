package com.example.payment.payment.topup;

import com.example.payment.card.Card;
import com.example.payment.card.CardRepository;
import com.example.payment.payment.entity.EntityStatus;
import com.example.payment.payment.entity.Wallet;
import com.example.payment.payment.exception.BusinessException;
import com.example.payment.payment.repository.WalletRepository;
import com.example.payment.payment.service.LedgerService;
import com.example.payment.otp.OtpPurpose;
import com.example.payment.otp.OtpService;
import com.example.payment.user.User;
import com.example.payment.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TopUpService {
    private final TopUpOrderRepository topUpOrderRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final WalletRepository walletRepository;
    private final LedgerService ledgerService;
    private final OtpService otpService;

    @Transactional
    public TopUpOrder execute(TopUpRequest request, String userId) {
        var existing = topUpOrderRepository.findById(request.topUpOrderId());
        if (existing.isPresent()) return existing.get();

        BigDecimal amount = parseAmount(request.amount());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "Không tìm thấy user"));
        if (user.getStatus() != EntityStatus.ACTIVE)
            throw BusinessException.badRequest("USER_INACTIVE", "User không hoạt động");
        otpService.assertVerified(request.otpId(), userId, OtpPurpose.TOP_UP, request.topUpOrderId());

        Card card = cardRepository.findById(request.cardId())
                .orElseThrow(() -> BusinessException.notFound("CARD_NOT_FOUND", "Không tìm thấy thẻ"));
        if (!card.getUserId().equals(userId))
            throw BusinessException.badRequest("CARD_NOT_OWNED", "Thẻ không thuộc user này");
        if (!card.isUsable())
            throw BusinessException.badRequest("CARD_INACTIVE", "Thẻ bị khóa hoặc hết hạn");

        Wallet wallet = walletRepository.findByCardIdForUpdate(card.getCardId())
                .orElseThrow(() -> BusinessException.notFound("WALLET_NOT_FOUND", "Thẻ chưa có ví"));

        TopUpOrder order = new TopUpOrder();
        order.setTopUpOrderId(request.topUpOrderId());
        order.setUserId(user.getUserId());
        order.setCardId(card.getCardId());
        order.setWalletId(wallet.getWalletId());
        order.setAmount(amount);
        order.setCurrency(request.currency().toUpperCase());
        order.setStatus(TopUpStatus.EXECUTING);
        topUpOrderRepository.save(order);

        if (!wallet.getCurrency().equalsIgnoreCase(request.currency()))
            return fail(order, "CURRENCY_MISMATCH");

        // Demo/internal top-up. A real system credits only after PSP confirmation.
        wallet.credit(amount);
        order.setWalletUpdated(true);
        ledgerService.recordTopUp(order.getTopUpOrderId(), card.getCardId(), amount, order.getCurrency());
        order.setLedgerUpdated(true);
        order.setStatus(TopUpStatus.SUCCESS);
        return order;
    }

    @Transactional(readOnly = true)
    public TopUpOrder get(String topUpOrderId) {
        return topUpOrderRepository.findById(topUpOrderId)
                .orElseThrow(() -> BusinessException.notFound("TOP_UP_NOT_FOUND", "Không tìm thấy top-up"));
    }

    private TopUpOrder fail(TopUpOrder order, String reason) {
        order.setStatus(TopUpStatus.FAILED);
        order.setFailureReason(reason);
        return order;
    }

    private BigDecimal parseAmount(String raw) {
        try {
            BigDecimal amount = new BigDecimal(raw);
            if (amount.signum() <= 0) throw new NumberFormatException();
            return amount;
        } catch (NumberFormatException | NullPointerException ex) {
            throw BusinessException.badRequest("INVALID_AMOUNT", "amount phải là số dương");
        }
    }
}
