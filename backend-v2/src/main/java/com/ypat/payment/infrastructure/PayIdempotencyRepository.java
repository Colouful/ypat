package com.ypat.payment.infrastructure;

import com.ypat.payment.domain.PayIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayIdempotencyRepository extends JpaRepository<PayIdempotency, String> {
}