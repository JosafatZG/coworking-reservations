package com.coworking.reservations.payment.client;

import com.coworking.reservations.payment.dto.PaymentValidationRequest;
import com.coworking.reservations.payment.dto.PaymentValidationResult;

public interface PaymentClient {

    PaymentValidationResult validatePayment(
            PaymentValidationRequest request
    );
}