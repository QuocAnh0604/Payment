package com.example.payment.payment.topup;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/v1/topups")
@RequiredArgsConstructor
public class TopUpController {
    private final TopUpService topUpService;

    @PostMapping
    public ResponseEntity<TopUpResponse> topUp(@Valid @RequestBody TopUpRequest request,
                                               Authentication authentication) {
        return ResponseEntity.ok(TopUpResponse.from(topUpService.execute(request, authentication.getName())));
    }

    @GetMapping("/{id}")
    public TopUpResponse get(@PathVariable String id) {
        return TopUpResponse.from(topUpService.get(id));
    }
}
