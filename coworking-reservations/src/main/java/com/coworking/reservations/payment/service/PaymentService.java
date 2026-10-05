package com.coworking.reservations.payment.service;

import com.coworking.reservations.payment.client.PaymentClient;
import com.coworking.reservations.payment.dto.PaymentValidationRequest;
import com.coworking.reservations.payment.dto.PaymentValidationResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String PAYMENT_INSTANCE = "paymentService";

    private final PaymentClient paymentClient;

    @CircuitBreaker(
            name = PAYMENT_INSTANCE,
            fallbackMethod = "validatePaymentFallback"
    )
    @TimeLimiter(name = PAYMENT_INSTANCE)
    public CompletableFuture<PaymentValidationResult> validatePayment(
            PaymentValidationRequest request
    ) {
        return CompletableFuture.supplyAsync(
                () -> paymentClient.validatePayment(request)
        );
    }

    private CompletableFuture<PaymentValidationResult>
    validatePaymentFallback(
            PaymentValidationRequest request,
            Throwable throwable
    ) {
        log.warn(
                "Payment validation unavailable for reservation {}. " +
                        "Keeping reservation in PENDING_PAYMENT",
                request.reservationId(),
                throwable
        );

        return CompletableFuture.completedFuture(
                new PaymentValidationResult(
                        false,
                        null
                )
        );
    }
}