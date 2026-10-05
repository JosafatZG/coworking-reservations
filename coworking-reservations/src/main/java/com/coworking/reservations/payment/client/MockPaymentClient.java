package com.coworking.reservations.payment.client;

import com.coworking.reservations.config.properties.PaymentMockProperties;
import com.coworking.reservations.payment.dto.PaymentValidationRequest;
import com.coworking.reservations.payment.dto.PaymentValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MockPaymentClient implements PaymentClient {

    private final PaymentMockProperties properties;

    @Override
    public PaymentValidationResult validatePayment(PaymentValidationRequest request) {
        return switch (properties.mode()) {
            case SUCCESS -> new PaymentValidationResult(
                    true,
                    "TX-" + UUID.randomUUID()
            );

            case FAILURE -> throw new IllegalStateException(
                    "Simulated payment provider failure"
            );

            case SLOW -> {
                simulateDelay();
                yield new PaymentValidationResult(
                        true,
                        "TX-" + UUID.randomUUID()
                );
            }
        };
    }

    private void simulateDelay() {
        if (properties.delay().isZero()) {
            return;
        }

        try {
            Thread.sleep(properties.delay().toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Payment provider call interrupted",
                    exception
            );
        }
    }
}