package com.coworking.reservations.reservation.repository;

import com.coworking.reservations.reservation.entity.Reservation;
import com.coworking.reservations.reservation.entity.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"space", "user"})
    List<Reservation> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"space", "user"})
    @NonNull
    Optional<Reservation> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"space", "user"})
    List<Reservation> findAllByOrderByStartDateTimeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.id = :id
        """)
    Optional<Reservation> findByIdForUpdate(
            @Param("id") Long id
    );

    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.space.id = :spaceId
          AND r.status IN :activeStatuses
          AND r.startDateTime < :endDateTime
          AND r.endDateTime > :startDateTime
        """)
    List<Reservation> findOverlappingReservations(
            @Param("spaceId") Long spaceId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("activeStatuses") List<ReservationStatus> activeStatuses
    );
}