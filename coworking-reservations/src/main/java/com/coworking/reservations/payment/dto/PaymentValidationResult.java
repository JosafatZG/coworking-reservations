package com.coworking.reservations.payment.dto;

public record PaymentValidationResult(
        boolean approved,
        String transactionId
) {
}