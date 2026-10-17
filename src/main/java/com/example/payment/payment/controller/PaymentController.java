package com.example.payment.payment.controller;

import com.example.payment.payment.dto.*;
import com.example.payment.payment.entity.PaymentOrder;
import com.example.payment.payment.repository.LedgerEntryRepository;
import com.example.payment.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final LedgerEntryRepository ledgerRepo;

    @PostMapping("/payments")
    public ResponseEntity<PaymentOrderResponse> pay(@Valid @RequestBody ExecutePaymentRequest req,
                                                    Authentication authentication) {
        PaymentOrder order = paymentService.execute(req, authentication.getName());
        return ResponseEntity.ok(PaymentOrderResponse.from(order));
    }

    @GetMapping("/payments/{id}")
    public PaymentOrderResponse get(@PathVariable String id) {
        return PaymentOrderResponse.from(paymentService.getOrder(id));
    }

    @GetMapping("/ledger/orders/{id}")
    public List<LedgerEntryResponse> ledgerOfOrder(@PathVariable String id) {
        return ledgerRepo.findByPaymentOrderId(id).stream().map(LedgerEntryResponse::from).toList();
    }

    @GetMapping("/ledger/cards/{cardId}/net")
    public Map<String, String> net(@PathVariable String cardId) {
        return Map.of("cardId", cardId, "net", ledgerRepo.netAmountByCard(cardId).toPlainString());
    }
}
