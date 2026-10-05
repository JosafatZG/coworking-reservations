package com.coworking.reservations.reservation.dto;

import com.coworking.reservations.reservation.entity.PaymentMethod;
import com.coworking.reservations.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long userId,
        Long spaceId,
        String spaceName,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        ReservationStatus status,
        PaymentMethod paymentMethod
) {
}