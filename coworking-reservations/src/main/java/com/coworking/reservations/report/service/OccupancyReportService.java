package com.coworking.reservations.report.service;

import com.coworking.reservations.report.dto.OccupancyReportResponse;
import com.coworking.reservations.reservation.entity.Reservation;
import com.coworking.reservations.reservation.entity.ReservationStatus;
import com.coworking.reservations.reservation.repository.ReservationRepository;
import com.coworking.reservations.space.entity.Space;
import com.coworking.reservations.space.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OccupancyReportService {

    private static final List<ReservationStatus> OCCUPIED_STATUSES = List.of(
            ReservationStatus.CONFIRMED,
            ReservationStatus.COMPLETED
    );

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;

    @Cacheable(
            cacheNames = "occupancyReports",
            key = "#startDateTime.toString() + '_' + #endDateTime.toString()"
    )
    public List<OccupancyReportResponse> generateReport(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        validateDateRange(startDateTime, endDateTime);

        List<Space> spaces = spaceRepository.findAllByOrderByNameAsc();

        List<Reservation> reservations =
                reservationRepository.findOverlappingByDateRange(
                        startDateTime,
                        endDateTime,
                        OCCUPIED_STATUSES
                );

        Map<Long, List<Reservation>> reservationsBySpace =
                reservations.stream()
                        .collect(Collectors.groupingBy(
                                reservation -> reservation.getSpace().getId()
                        ));

        long totalMinutes = Duration.between(
                startDateTime,
                endDateTime
        ).toMinutes();

        return spaces.stream()
                .map(space -> toResponse(
                        space,
                        reservationsBySpace.getOrDefault(space.getId(), List.of()),
                        startDateTime,
                        endDateTime,
                        totalMinutes
                ))
                .toList();
    }

    private OccupancyReportResponse toResponse(
            Space space,
            List<Reservation> reservations,
            LocalDateTime reportStart,
            LocalDateTime reportEnd,
            long totalMinutes
    ) {
        long occupiedMinutes = reservations.stream()
                .mapToLong(reservation ->
                        calculateOverlapMinutes(
                                reservation,
                                reportStart,
                                reportEnd
                        )
                )
                .sum();

        BigDecimal occupancyPercentage = BigDecimal.valueOf(occupiedMinutes)
                .multiply(ONE_HUNDRED)
                .divide(
                        BigDecimal.valueOf(totalMinutes),
                        2,
                        RoundingMode.HALF_UP
                );

        return new OccupancyReportResponse(
                space.getId(),
                space.getName(),
                space.getType(),
                occupancyPercentage
        );
    }

    private long calculateOverlapMinutes(
            Reservation reservation,
            LocalDateTime reportStart,
            LocalDateTime reportEnd
    ) {
        LocalDateTime overlapStart = reservation.getStartDateTime().isAfter(reportStart)
                ? reservation.getStartDateTime()
                : reportStart;

        LocalDateTime overlapEnd = reservation.getEndDateTime().isBefore(reportEnd)
                ? reservation.getEndDateTime()
                : reportEnd;

        if (!overlapStart.isBefore(overlapEnd)) {
            return 0;
        }

        return Duration.between(overlapStart, overlapEnd).toMinutes();
    }

    private void validateDateRange(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        if (!endDateTime.isAfter(startDateTime)) {
            throw new IllegalArgumentException(
                    "End date-time must be after start date-time"
            );
        }
    }
}