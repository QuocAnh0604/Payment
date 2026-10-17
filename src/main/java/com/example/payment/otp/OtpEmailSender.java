package com.example.payment.otp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OtpEmailSender {
    private final JavaMailSender mailSender;
    private final String from;

    public OtpEmailSender(JavaMailSender mailSender,
                          @Value("${SMTP_FROM:${spring.mail.username:}}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void send(String recipient, String code, LocalDateTime expiresAt) {
        if (from == null || from.isBlank())
            throw new IllegalStateException("SMTP_FROM or SMTP_USERNAME must be configured");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("Payment System - OTP verification code");
        message.setText("Your OTP code is: " + code + "\n\n"
                + "This code expires at " + expiresAt + ".\n"
                + "Do not share this code with anyone.");
        mailSender.send(message);
    }
}
