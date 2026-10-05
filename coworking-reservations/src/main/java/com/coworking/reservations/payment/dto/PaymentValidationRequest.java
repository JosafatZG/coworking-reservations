package com.coworking.reservations.payment.dto;

import com.coworking.reservations.reservation.entity.PaymentMethod;

import java.math.BigDecimal;

public record PaymentValidationRequest(
        Long reservationId,
        BigDecimal amount,
        PaymentMethod paymentMethod
) {
}