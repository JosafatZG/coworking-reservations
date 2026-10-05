package com.coworking.reservations.report.service;

import com.coworking.reservations.report.dto.OccupancyReportResponse;
import com.coworking.reservations.reservation.entity.Reservation;
import com.coworking.reservations.reservation.entity.ReservationStatus;
import com.coworking.reservations.reservation.repository.ReservationRepository;
import com.coworking.reservations.space.entity.Space;
import com.coworking.reservations.space.entity.SpaceType;
import com.coworking.reservations.space.repository.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OccupancyReportServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @InjectMocks
    private OccupancyReportService occupancyReportService;

    private LocalDateTime reportStart;
    private LocalDateTime reportEnd;

    @BeforeEach
    void setUp() {
        reportStart = LocalDateTime.of(2026, 10, 5, 8, 0);
        reportEnd = LocalDateTime.of(2026, 10, 5, 18, 0);
    }

    @Test
    void shouldCalculateOccupancyForReservationInsideRange() {
        Space space = space(1L, "Meeting Room A");

        Reservation reservation = reservation(
                space,
                LocalDateTime.of(2026, 10, 5, 10, 0),
                LocalDateTime.of(2026, 10, 5, 12, 0)
        );

        when(spaceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(space));

        when(reservationRepository.findOverlappingByDateRange(
                eq(reportStart),
                eq(reportEnd),
                eq(List.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED
                ))
        )).thenReturn(List.of(reservation));

        List<OccupancyReportResponse> result =
                occupancyReportService.generateReport(reportStart, reportEnd);

        assertEquals(1, result.size());
        assertEquals(
                new BigDecimal("20.00"),
                result.getFirst().occupancyPercentage()
        );
    }

    @Test
    void shouldCalculateOnlyIntersectionWithReportRange() {
        Space space = space(1L, "Meeting Room A");

        Reservation reservation = reservation(
                space,
                LocalDateTime.of(2026, 10, 5, 7, 0),
                LocalDateTime.of(2026, 10, 5, 10, 0)
        );

        when(spaceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(space));

        when(reservationRepository.findOverlappingByDateRange(
                eq(reportStart),
                eq(reportEnd),
                eq(List.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED
                ))
        )).thenReturn(List.of(reservation));

        List<OccupancyReportResponse> result =
                occupancyReportService.generateReport(reportStart, reportEnd);

        assertEquals(
                new BigDecimal("20.00"),
                result.getFirst().occupancyPercentage()
        );
    }

    @Test
    void shouldExcludePendingPaymentReservations() {
        Space space = space(1L, "Meeting Room A");

        when(spaceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(space));

        when(reservationRepository.findOverlappingByDateRange(
                eq(reportStart),
                eq(reportEnd),
                eq(List.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED
                ))
        )).thenReturn(List.of());

        List<OccupancyReportResponse> result =
                occupancyReportService.generateReport(reportStart, reportEnd);

        assertEquals(
                new BigDecimal("0.00"),
                result.getFirst().occupancyPercentage()
        );
    }

    @Test
    void shouldIncludeCompletedReservations() {
        Space space = space(1L, "Meeting Room A");

        Reservation reservation = reservation(
                space,
                LocalDateTime.of(2026, 10, 5, 10, 0),
                LocalDateTime.of(2026, 10, 5, 12, 0)
        );

        when(spaceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(space));

        when(reservationRepository.findOverlappingByDateRange(
                eq(reportStart),
                eq(reportEnd),
                eq(List.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED
                ))
        )).thenReturn(List.of(reservation));

        List<OccupancyReportResponse> result =
                occupancyReportService.generateReport(reportStart, reportEnd);

        assertEquals(
                new BigDecimal("20.00"),
                result.getFirst().occupancyPercentage()
        );
    }

    @Test
    void shouldReturnZeroOccupancyWhenSpaceHasNoReservations() {
        Space space = space(1L, "Meeting Room A");

        when(spaceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(space));

        when(reservationRepository.findOverlappingByDateRange(
                eq(reportStart),
                eq(reportEnd),
                eq(List.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED
                ))
        )).thenReturn(List.of());

        List<OccupancyReportResponse> result =
                occupancyReportService.generateReport(reportStart, reportEnd);

        assertEquals(
                new BigDecimal("0.00"),
                result.getFirst().occupancyPercentage()
        );
    }

    @Test
    void shouldReturnReportForEverySpace() {
        Space firstSpace = space(1L, "Meeting Room A");
        Space secondSpace = space(2L, "Meeting Room B");

        when(spaceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(firstSpace, secondSpace));

        when(reservationRepository.findOverlappingByDateRange(
                eq(reportStart),
                eq(reportEnd),
                eq(List.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED
                ))
        )).thenReturn(List.of());

        List<OccupancyReportResponse> result =
                occupancyReportService.generateReport(reportStart, reportEnd);

        assertEquals(2, result.size());
        assertEquals("Meeting Room A", result.get(0).spaceName());
        assertEquals("Meeting Room B", result.get(1).spaceName());
    }

    @Test
    void shouldGroupReservationsBySpace() {
        Space firstSpace = space(1L, "Meeting Room A");
        Space secondSpace = space(2L, "Meeting Room B");

        Reservation firstReservation = reservation(
                firstSpace,
                LocalDateTime.of(2026, 10, 5, 8, 0),
                LocalDateTime.of(2026, 10, 5, 10, 0)
        );

        Reservation secondReservation = reservation(
                secondSpace,
                LocalDateTime.of(2026, 10, 5, 12, 0),
                LocalDateTime.of(2026, 10, 5, 15, 0)
        );

        when(spaceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(firstSpace, secondSpace));

        when(reservationRepository.findOverlappingByDateRange(
                eq(reportStart),
                eq(reportEnd),
                eq(List.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED
                ))
        )).thenReturn(List.of(firstReservation, secondReservation));

        List<OccupancyReportResponse> result =
                occupancyReportService.generateReport(reportStart, reportEnd);

        assertEquals(new BigDecimal("20.00"), result.get(0).occupancyPercentage());
        assertEquals(new BigDecimal("30.00"), result.get(1).occupancyPercentage());
    }

    @Test
    void shouldCalculateOccupancyWithMinutePrecision() {
        Space space = space(1L, "Meeting Room A");

        Reservation reservation = reservation(
                space,
                LocalDateTime.of(2026, 10, 5, 10, 15),
                LocalDateTime.of(2026, 10, 5, 11, 45)
        );

        when(spaceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(space));

        when(reservationRepository.findOverlappingByDateRange(
                eq(reportStart),
                eq(reportEnd),
                eq(List.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED
                ))
        )).thenReturn(List.of(reservation));

        List<OccupancyReportResponse> result =
                occupancyReportService.generateReport(reportStart, reportEnd);

        assertEquals(
                new BigDecimal("15.00"),
                result.getFirst().occupancyPercentage()
        );
    }

    @Test
    void shouldRejectInvalidDateRange() {
        LocalDateTime invalidEnd = reportStart.minusHours(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> occupancyReportService.generateReport(
                        reportStart,
                        invalidEnd
                )
        );
    }

    @Test
    void shouldPassOnlyOccupiedStatusesToRepository() {
        when(spaceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of());

        when(reservationRepository.findOverlappingByDateRange(
                eq(reportStart),
                eq(reportEnd),
                eq(List.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED
                ))
        )).thenReturn(List.of());

        occupancyReportService.generateReport(reportStart, reportEnd);

        ArgumentCaptor<Collection<ReservationStatus>> statusesCaptor =
                ArgumentCaptor.forClass(Collection.class);

        verify(reservationRepository).findOverlappingByDateRange(
                eq(reportStart),
                eq(reportEnd),
                statusesCaptor.capture()
        );

        assertEquals(
                List.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED
                ),
                statusesCaptor.getValue()
        );
    }

    private Space space(Long id, String name) {
        Space space = org.mockito.Mockito.mock(Space.class);
        when(space.getId()).thenReturn(id);
        when(space.getName()).thenReturn(name);
        when(space.getType()).thenReturn(SpaceType.MEETING_ROOM);
        return space;
    }

    private Reservation reservation(
            Space space,
            LocalDateTime start,
            LocalDateTime end
    ) {
        Reservation reservation = org.mockito.Mockito.mock(Reservation.class);

        when(reservation.getSpace()).thenReturn(space);
        when(reservation.getStartDateTime()).thenReturn(start);
        when(reservation.getEndDateTime()).thenReturn(end);

        return reservation;
    }
}