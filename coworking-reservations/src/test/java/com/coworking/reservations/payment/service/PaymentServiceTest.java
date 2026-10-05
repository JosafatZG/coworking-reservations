package com.coworking.reservations.payment.service;

import com.coworking.reservations.payment.client.PaymentClient;
import com.coworking.reservations.payment.dto.PaymentValidationRequest;
import com.coworking.reservations.payment.dto.PaymentValidationResult;
import com.coworking.reservations.reservation.entity.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentClient paymentClient;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentClient);
    }

    @Test
    void shouldReturnApprovedPaymentResult() throws Exception {
        PaymentValidationRequest request = new PaymentValidationRequest(
                1L,
                BigDecimal.valueOf(50),
                PaymentMethod.CREDIT_CARD
        );

        PaymentValidationResult expected = new PaymentValidationResult(
                true,
                "TX-123"
        );

        when(paymentClient.validatePayment(request))
                .thenReturn(expected);

        PaymentValidationResult result = paymentService
                .validatePayment(request)
                .get();

        assertThat(result).isEqualTo(expected);

        verify(paymentClient).validatePayment(request);
    }

    @Test
    void shouldPropagatePaymentProviderFailure() {
        PaymentValidationRequest request = new PaymentValidationRequest(
                1L,
                BigDecimal.valueOf(50),
                PaymentMethod.CREDIT_CARD
        );

        RuntimeException exception =
                new RuntimeException("Payment provider unavailable");

        when(paymentClient.validatePayment(request))
                .thenThrow(exception);

        CompletableFuture<PaymentValidationResult> future =
                paymentService.validatePayment(request);

        assertThatThrownBy(future::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(exception);

        verify(paymentClient).validatePayment(request);
    }
}