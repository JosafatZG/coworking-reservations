package com.coworking.reservations.reservation.event;

public record ReservationConfirmedEvent(
        Long reservationId,
        String userEmail,
        String spaceName
) {
}