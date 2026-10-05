package com.coworking.reservations.reservation.service;

import com.coworking.reservations.common.exception.ResourceNotFoundException;
import com.coworking.reservations.payment.dto.PaymentValidationRequest;
import com.coworking.reservations.payment.dto.PaymentValidationResult;
import com.coworking.reservations.payment.service.PaymentService;
import com.coworking.reservations.reservation.dto.CreateReservationRequest;
import com.coworking.reservations.reservation.dto.ReservationResponse;
import com.coworking.reservations.reservation.entity.Reservation;
import com.coworking.reservations.reservation.repository.ReservationRepository;
import com.coworking.reservations.user.entity.Role;
import com.coworking.reservations.user.entity.User;
import com.coworking.reservations.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ReservationTransactionService reservationTransactionService;
    private final PaymentService paymentService;

    public ReservationResponse create(
            CreateReservationRequest request,
            Authentication authentication
    ) {
        validateDateRange(request);

        User user = findUser(authentication.getName());

        ReservationPaymentContext context =
                reservationTransactionService.createPendingReservation(
                        request,
                        user
                );

        PaymentValidationResult paymentResult =
                paymentService.validatePayment(
                        new PaymentValidationRequest(
                                context.reservationId(),
                                context.amount(),
                                request.paymentMethod()
                        )
                ).join();

        if (!paymentResult.approved()) {
            return context.response();
        }

        return reservationTransactionService.confirmReservation(
                context.reservationId()
        );
    }

    public List<ReservationResponse> findAll(
            Authentication authentication
    ) {
        User user = findUser(authentication.getName());

        return reservationRepository.findByUserId(user.getId())
                .stream()
                .map(reservationTransactionService::toResponse)
                .toList();
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllByOrderByStartDateTimeAsc()
                .stream()
                .map(reservationTransactionService::toResponse)
                .toList();
    }

    public ReservationResponse findById(
            Long id,
            Authentication authentication
    ) {
        User user = findUser(authentication.getName());

        Reservation reservation = findReservation(id);

        validateAccess(reservation.getUser().getId(), user);

        return reservationTransactionService.toResponse(reservation);
    }

    public void cancel(
            Long id,
            Authentication authentication
    ) {
        User user = findUser(authentication.getName());

        reservationTransactionService.cancelReservation(
                id,
                user
        );
    }

    private void validateDateRange(CreateReservationRequest request) {
        if (!request.endDateTime().isAfter(request.startDateTime())) {
            throw new InvalidReservationException(
                    "End date/time must be after start date/time"
            );
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with email '%s' not found"
                                        .formatted(email)
                        )
                );
    }

    private Reservation
    findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation with id %d not found"
                                        .formatted(id)
                        )
                );
    }

    private void validateAccess(Long reservationUserId, User user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (!reservationUserId.equals(user.getId())) {
            throw new com.coworking.reservations.common.exception
                    .ResourceConflictException(
                    "You do not have access to this reservation"
            );
        }
    }
}