package com.coworking.reservations.reservation.repository;

import com.coworking.reservations.reservation.entity.Reservation;
import com.coworking.reservations.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

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
            @Param("activeStatuses") List<ReservationStatus> activeStatuses);
}