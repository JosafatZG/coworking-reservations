package com.coworking.reservations.reservation.service;

import com.coworking.reservations.common.exception.ResourceConflictException;
import com.coworking.reservations.common.exception.ResourceNotFoundException;
import com.coworking.reservations.reservation.dto.CreateReservationRequest;
import com.coworking.reservations.reservation.dto.ReservationResponse;
import com.coworking.reservations.reservation.entity.Reservation;
import com.coworking.reservations.reservation.entity.ReservationStatus;
import com.coworking.reservations.reservation.mapper.ReservationMapper;
import com.coworking.reservations.reservation.repository.ReservationRepository;
import com.coworking.reservations.space.entity.Space;
import com.coworking.reservations.space.repository.SpaceRepository;
import com.coworking.reservations.user.entity.Role;
import com.coworking.reservations.user.entity.User;
import com.coworking.reservations.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES = List.of(
            ReservationStatus.PENDING_PAYMENT,
            ReservationStatus.CONFIRMED
    );

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final ReservationMapper reservationMapper;

    @Transactional
    public ReservationResponse create(
            CreateReservationRequest request,
            Authentication authentication
    ) {
        validateDateRange(request);

        User user = findUser(authentication.getName());
        Space space = findSpaceForUpdate(request.spaceId());

        validateNoOverlap(request, space);

        Reservation reservation = reservationMapper.toEntity(
                request,
                user,
                space
        );

        Reservation savedReservation = reservationRepository.save(reservation);

        return reservationMapper.toResponse(savedReservation);
    }

    public List<ReservationResponse> findAll(Authentication authentication) {

        User user = findUser(authentication.getName());

        return reservationRepository.findByUserId(user.getId())
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllByOrderByStartDateTimeAsc()
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    public ReservationResponse findById(
            Long id,
            Authentication authentication
    ) {
        User user = findUser(authentication.getName());

        Reservation reservation = findReservation(id);

        validateAccess(reservation, user);

        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public void cancel(
            Long id,
            Authentication authentication
    ) {
        User user = findUser(authentication.getName());

        Reservation reservation = findReservation(id);

        validateAccess(reservation, user);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ResourceConflictException(
                    "Reservation with id %d is already cancelled".formatted(id)
            );
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new ResourceConflictException(
                    "Completed reservations cannot be cancelled"
            );
        }

        reservation.cancel();
    }

    private void validateDateRange(CreateReservationRequest request) {

        if (!request.endDateTime().isAfter(request.startDateTime())) {
            throw new InvalidReservationException(
                    "End date/time must be after start date/time"
            );
        }
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

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with email '%s' not found".formatted(email)
                        )
                );
    }

    private Space findSpaceForUpdate(Long id) {
        return spaceRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Space with id %d not found".formatted(id)
                        )
                );
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation with id %d not found".formatted(id)
                        )
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