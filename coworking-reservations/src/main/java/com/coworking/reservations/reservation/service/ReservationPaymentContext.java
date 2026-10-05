package com.coworking.reservations.reservation.service;

import com.coworking.reservations.reservation.dto.ReservationResponse;

import java.math.BigDecimal;

public record ReservationPaymentContext(
        Long reservationId,
        BigDecimal amount,
        ReservationResponse response
) {
}