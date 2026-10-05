package com.coworking.reservations.reservation.service;

import com.coworking.reservations.common.exception.ResourceConflictException;
import com.coworking.reservations.common.exception.ResourceNotFoundException;
import com.coworking.reservations.reservation.dto.CreateReservationRequest;
import com.coworking.reservations.reservation.dto.ReservationResponse;
import com.coworking.reservations.reservation.entity.Reservation;
import com.coworking.reservations.reservation.entity.ReservationStatus;
import com.coworking.reservations.reservation.event.ReservationConfirmedEvent;
import com.coworking.reservations.reservation.mapper.ReservationMapper;
import com.coworking.reservations.reservation.repository.ReservationRepository;
import com.coworking.reservations.space.entity.Space;
import com.coworking.reservations.space.repository.SpaceRepository;
import com.coworking.reservations.user.entity.Role;
import com.coworking.reservations.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationTransactionService {

    private static final List<ReservationStatus> ACTIVE_STATUSES = List.of(
            ReservationStatus.PENDING_PAYMENT,
            ReservationStatus.CONFIRMED
    );

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final ReservationMapper reservationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReservationPaymentContext createPendingReservation(
            CreateReservationRequest request,
            User user
    ) {
        Space space = findSpaceForUpdate(request.spaceId());

        validateNoOverlap(request, space);

        Reservation reservation = reservationMapper.toEntity(
                request,
                user,
                space
        );

        Reservation savedReservation = reservationRepository.save(reservation);

        BigDecimal amount = calculateAmount(
                space,
                request.startDateTime(),
                request.endDateTime()
        );

        ReservationResponse response =
                reservationMapper.toResponse(savedReservation);

        return new ReservationPaymentContext(
                savedReservation.getId(),
                amount,
                response
        );
    }

    @CacheEvict(
            cacheNames = "occupancyReports",
            allEntries = true
    )
    @Transactional
    public ReservationResponse confirmReservation(Long reservationId) {

        Reservation reservation = reservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation with id %d not found"
                                        .formatted(reservationId)
                        )
                );

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new ResourceConflictException(
                    "Reservation with id %d is no longer pending payment"
                            .formatted(reservationId)
            );
        }

        reservation.confirm();

        eventPublisher.publishEvent(
                new ReservationConfirmedEvent(
                        reservation.getId(),
                        reservation.getUser().getEmail(),
                        reservation.getSpace().getName()
                )
        );

        return reservationMapper.toResponse(reservation);
    }

    @CacheEvict(
            cacheNames = "occupancyReports",
            allEntries = true
    )
    @Transactional
    public void cancelReservation(Long reservationId, User user) {
        Reservation reservation = reservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation with id %d not found"
                                        .formatted(reservationId)
                        )
                );

        validateAccess(reservation, user);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ResourceConflictException(
                    "Reservation with id %d is already cancelled"
                            .formatted(reservationId)
            );
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new ResourceConflictException(
                    "Completed reservations cannot be cancelled"
            );
        }

        reservation.cancel();
    }

    public ReservationResponse toResponse(Reservation reservation) {
        return reservationMapper.toResponse(reservation);
    }

    private Space findSpaceForUpdate(Long id) {
        return spaceRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Space with id %d not found"
                                        .formatted(id)
                        )
                );
    }

    private void validateNoOverlap(
            CreateReservationRequest request,
            Space space
    ) {
        boolean hasOverlap = !reservationRepository
                .findOverlappingReservations(
                        space.getId(),
                        request.startDateTime(),
                        request.endDateTime(),
                        ACTIVE_STATUSES
                )
                .isEmpty();

        if (hasOverlap) {
            throw new ResourceConflictException(
                    "Space '%s' is already reserved for the requested time range"
                            .formatted(space.getName())
            );
        }
    }

    private BigDecimal calculateAmount(
            Space space,
            LocalDateTime start,
            LocalDateTime end
    ) {
        long minutes = Duration.between(start, end).toMinutes();

        return space.getHourlyRate()
                .multiply(BigDecimal.valueOf(minutes))
                .divide(
                        BigDecimal.valueOf(60),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private void validateAccess(
            Reservation reservation,
            User user
    ) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResourceConflictException(
                    "You do not have access to this reservation"
            );
        }
    }
}