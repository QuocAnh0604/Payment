package com.example.payment.otp;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @PostMapping("/challenges")
    public OtpChallengeResponse create(@Valid @RequestBody OtpCreateRequest request,
                                       Authentication authentication) {
        return OtpChallengeResponse.from(otpService.create(request, authentication.getName()));
    }

    @PostMapping("/challenges/{otpId}/verify")
    public OtpChallengeResponse verify(@PathVariable UUID otpId,
                                       @Valid @RequestBody OtpVerifyRequest request,
                                       Authentication authentication) {
        return OtpChallengeResponse.from(otpService.verify(otpId, request, authentication.getName()));
    }
}
