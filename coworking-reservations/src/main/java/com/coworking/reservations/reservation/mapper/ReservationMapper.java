package com.coworking.reservations.reservation.mapper;

import com.coworking.reservations.reservation.dto.CreateReservationRequest;
import com.coworking.reservations.reservation.dto.ReservationResponse;
import com.coworking.reservations.reservation.entity.Reservation;
import com.coworking.reservations.space.entity.Space;
import com.coworking.reservations.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public Reservation toEntity(
            CreateReservationRequest request,
            User user,
            Space space
    ) {
        return new Reservation(
                user,
                space,
                request.startDateTime(),
                request.endDateTime(),
                request.paymentMethod()
        );
    }

    public ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getSpace().getId(),
                reservation.getSpace().getName(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime(),
                reservation.getStatus(),
                reservation.getPaymentMethod()
        );
    }
}