package com.coworking.reservations.reservation.dto;

import com.coworking.reservations.reservation.entity.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationRequest(

        @NotNull
        Long spaceId,

        @NotNull
        @Future
        LocalDateTime startDateTime,

        @NotNull
        @Future
        LocalDateTime endDateTime,

        @NotNull
        PaymentMethod paymentMethod

) {
}