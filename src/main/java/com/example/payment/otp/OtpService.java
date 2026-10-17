package com.example.payment.otp;

import com.example.payment.payment.entity.EntityStatus;
import com.example.payment.payment.entity.PaymentEvent;
import com.example.payment.payment.entity.PaymentOrder;
import com.example.payment.payment.exception.BusinessException;
import com.example.payment.payment.topup.TopUpOrder;
import com.example.payment.payment.topup.TopUpOrderRepository;
import com.example.payment.payment.repository.PaymentEventRepository;
import com.example.payment.payment.repository.PaymentOrderRepository;
import com.example.payment.user.User;
import com.example.payment.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class OtpService {
    private static final int OTP_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;
    private static final int TTL_MINUTES = 5;

    private final OtpChallengeRepository otpRepository;
    private final UserRepository userRepository;
    private final TopUpOrderRepository topUpOrderRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final OtpEmailSender otpEmailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String secret;

    public OtpService(OtpChallengeRepository otpRepository, UserRepository userRepository,
                      TopUpOrderRepository topUpOrderRepository, PaymentOrderRepository paymentOrderRepository,
                      PaymentEventRepository paymentEventRepository,
                      OtpEmailSender otpEmailSender,
                      @Value("${app.otp.secret-key}") String secret) {
        if (secret == null || secret.length() < 32)
            throw new IllegalArgumentException("app.otp.secret-key must be at least 32 characters");
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.topUpOrderRepository = topUpOrderRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentEventRepository = paymentEventRepository;
        this.otpEmailSender = otpEmailSender;
        this.secret = secret;
    }

    @Transactional
    public OtpChallenge create(OtpCreateRequest request, String userId) {
        User user = activeUser(userId);
        if (request.channel() != OtpChannel.EMAIL)
            throw BusinessException.badRequest("OTP_CHANNEL_UNSUPPORTED", "Hiện chỉ hỗ trợ OTP qua email");
        validateReferenceOwnership(request.purpose(), request.referenceId(), userId);

        otpRepository.findTopByUserIdAndPurposeAndReferenceIdAndUsedAtIsNullOrderByCreatedAtDesc(
                userId, request.purpose(), request.referenceId()).ifPresent(previous -> {
            previous.setUsedAt(LocalDateTime.now());
        });

        OtpChallenge challenge = new OtpChallenge();
        challenge.setOtpId(UUID.randomUUID());
        challenge.setUserId(userId);
        challenge.setPurpose(request.purpose());
        challenge.setReferenceId(request.referenceId());
        challenge.setChannel(request.channel());
        challenge.setRecipientHint(maskRecipient(request.channel(), user));
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(TTL_MINUTES));
        challenge.setAttemptCount(0);
        challenge.setMaxAttempts(MAX_ATTEMPTS);

        String code = generateCode();
        challenge.setOtpHash(hash(challenge.getOtpId(), code));
        OtpChallenge saved = otpRepository.save(challenge);
        otpEmailSender.send(user.getEmail(), code, saved.getExpiresAt());
        return saved;
    }

    @Transactional
    public OtpChallenge verify(UUID otpId, OtpVerifyRequest request, String userId) {
        OtpChallenge challenge = otpRepository.findById(otpId)
                .orElseThrow(() -> BusinessException.notFound("OTP_NOT_FOUND", "Không tìm thấy OTP"));
        if (!challenge.getUserId().equals(userId))
            throw BusinessException.badRequest("OTP_NOT_OWNED", "OTP không thuộc user này");
        if (challenge.getUsedAt() != null)
            throw BusinessException.badRequest("OTP_ALREADY_USED", "OTP đã được sử dụng");
        if (!challenge.getExpiresAt().isAfter(LocalDateTime.now()))
            throw BusinessException.badRequest("OTP_EXPIRED", "OTP đã hết hạn");
        if (challenge.getAttemptCount() >= challenge.getMaxAttempts())
            throw BusinessException.badRequest("OTP_ATTEMPTS_EXCEEDED", "OTP đã vượt quá số lần thử");

        challenge.setAttemptCount(challenge.getAttemptCount() + 1);
        if (!MessageDigest.isEqual(hex(hash(challenge.getOtpId(), request.code())), hex(challenge.getOtpHash())))
            throw BusinessException.badRequest("OTP_INVALID", "OTP không đúng");

        challenge.setUsedAt(LocalDateTime.now());
        return challenge;
    }

    @Transactional
    public void assertVerified(UUID otpId, String userId, OtpPurpose purpose, String referenceId) {
        if (otpId == null)
            throw BusinessException.badRequest("OTP_REQUIRED", "otpId là bắt buộc");
        OtpChallenge challenge = otpRepository.findById(otpId)
                .orElseThrow(() -> BusinessException.notFound("OTP_NOT_FOUND", "Không tìm thấy OTP"));
        if (!challenge.getUserId().equals(userId)
                || challenge.getPurpose() != purpose
                || !challenge.getReferenceId().equals(referenceId))
            throw BusinessException.badRequest("OTP_MISMATCH", "OTP không khớp với giao dịch");
        if (challenge.getUsedAt() == null)
            throw BusinessException.badRequest("OTP_NOT_VERIFIED", "OTP chưa được xác thực");
        if (!challenge.getExpiresAt().isAfter(LocalDateTime.now()))
            throw BusinessException.badRequest("OTP_EXPIRED", "OTP đã hết hạn");
    }

    private User activeUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "Không tìm thấy user"));
        if (user.getStatus() != EntityStatus.ACTIVE)
            throw BusinessException.badRequest("USER_INACTIVE", "User không hoạt động");
        return user;
    }

    private void validateReferenceOwnership(OtpPurpose purpose, String referenceId, String userId) {
        if (purpose == OtpPurpose.TOP_UP) {
            TopUpOrder order = topUpOrderRepository.findById(referenceId).orElse(null);
            if (order != null && !order.getUserId().equals(userId)) throw notOwned();
        } else if (purpose == OtpPurpose.TRANSFER) {
            PaymentOrder order = paymentOrderRepository.findById(referenceId).orElse(null);
            if (order != null) {
                PaymentEvent event = paymentEventRepository.findById(order.getCheckoutId())
                        .orElseThrow(() -> BusinessException.notFound("EVENT_NOT_FOUND", "Không tìm thấy payment event"));
                if (!event.getUserId().equals(userId)) throw notOwned();
            }
        } else if (!referenceId.equals(userId)) {
            throw notOwned();
        }
    }

    private BusinessException notOwned() {
        return BusinessException.badRequest("REFERENCE_NOT_OWNED", "Giao dịch không thuộc user này");
    }

    private String generateCode() {
        return String.format("%0" + OTP_LENGTH + "d", secureRandom.nextInt(1_000_000));
    }

    private String hash(UUID otpId, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal((otpId + ":" + code).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash OTP", ex);
        }
    }

    private byte[] hex(String value) {
        return HexFormat.of().parseHex(value);
    }

    private String maskRecipient(OtpChannel channel, User user) {
        String recipient = channel == OtpChannel.SMS ? user.getPhone() : user.getEmail();
        if (recipient == null || recipient.isBlank())
            throw BusinessException.badRequest("OTP_RECIPIENT_MISSING", "User chưa có thông tin nhận OTP");
        if (channel == OtpChannel.SMS)
            return recipient.length() <= 4 ? "****" : "****" + recipient.substring(recipient.length() - 4);
        int at = recipient.indexOf('@');
        return at <= 1 ? "***" + recipient.substring(Math.max(at, 0))
                : recipient.charAt(0) + "***" + recipient.substring(at);
    }
}
