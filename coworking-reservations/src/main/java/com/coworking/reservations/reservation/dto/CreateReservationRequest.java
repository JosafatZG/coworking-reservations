package com.coworking.reservations.reservation.dto;

import com.coworking.reservations.reservation.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationRequest(

        @NotNull
        @Schema(
                description = "Identifier of the coworking space to reserve",
                example = "1"
        )
        Long spaceId,

        @NotNull
        @Future
        @Schema(
                description = "Reservation start date and time",
                example = "2026-10-05T13:00:00"
        )
        LocalDateTime startDateTime,

        @NotNull
        @Future
        @Schema(
                description = "Reservation end date and time",
                example = "2026-10-05T15:00:00"
        )
        LocalDateTime endDateTime,

        @NotNull
        @Schema(
                description = "Payment method used for the reservation",
                example = "CREDIT_CARD"
        )
        PaymentMethod paymentMethod

) {
}