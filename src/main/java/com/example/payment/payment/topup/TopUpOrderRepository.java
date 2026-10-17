package com.example.payment.payment.topup;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TopUpOrderRepository extends JpaRepository<TopUpOrder, String> {
}
