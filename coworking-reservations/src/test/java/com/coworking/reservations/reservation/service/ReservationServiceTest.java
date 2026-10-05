package com.coworking.reservations.reservation.service;

import com.coworking.reservations.common.exception.ResourceConflictException;
import com.coworking.reservations.common.exception.ResourceNotFoundException;
import com.coworking.reservations.payment.dto.PaymentValidationRequest;
import com.coworking.reservations.payment.dto.PaymentValidationResult;
import com.coworking.reservations.payment.service.PaymentService;
import com.coworking.reservations.reservation.dto.CreateReservationRequest;
import com.coworking.reservations.reservation.dto.ReservationResponse;
import com.coworking.reservations.reservation.entity.PaymentMethod;
import com.coworking.reservations.reservation.entity.Reservation;
import com.coworking.reservations.reservation.entity.ReservationStatus;
import com.coworking.reservations.reservation.repository.ReservationRepository;
import com.coworking.reservations.user.entity.Role;
import com.coworking.reservations.user.entity.User;
import com.coworking.reservations.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationTransactionService reservationTransactionService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private Authentication authentication;

    @Mock
    private User user;

    @Mock
    private Reservation reservation;

    @InjectMocks
    private ReservationService reservationService;

    private CreateReservationRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateReservationRequest(
                1L,
                LocalDateTime.of(2026, 10, 5, 10, 0),
                LocalDateTime.of(2026, 10, 5, 12, 0),
                PaymentMethod.CREDIT_CARD
        );
    }

    @Test
    void shouldCreateAndConfirmReservationWhenPaymentIsApproved() {
        ReservationPaymentContext context = new ReservationPaymentContext(
                10L,
                new BigDecimal("25.00"),
                response()
        );

        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(reservationTransactionService.createPendingReservation(
                request,
                user
        )).thenReturn(context);

        when(paymentService.validatePayment(any(PaymentValidationRequest.class)))
                .thenReturn(
                        CompletableFuture.completedFuture(
                                new PaymentValidationResult(true, "TX-123")
                        )
                );

        when(reservationTransactionService.confirmReservation(10L))
                .thenReturn(response());

        ReservationResponse result =
                reservationService.create(request, authentication);

        assertEquals(response(), result);

        verify(reservationTransactionService)
                .createPendingReservation(request, user);

        verify(paymentService)
                .validatePayment(any(PaymentValidationRequest.class));

        verify(reservationTransactionService)
                .confirmReservation(10L);
    }

    @Test
    void shouldKeepReservationPendingWhenPaymentIsRejected() {
        ReservationResponse pendingResponse = response();

        ReservationPaymentContext context = new ReservationPaymentContext(
                10L,
                new BigDecimal("25.00"),
                pendingResponse
        );

        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(reservationTransactionService.createPendingReservation(
                request,
                user
        )).thenReturn(context);

        when(paymentService.validatePayment(any(PaymentValidationRequest.class)))
                .thenReturn(
                        CompletableFuture.completedFuture(
                                new PaymentValidationResult(false, null)
                        )
                );

        ReservationResponse result =
                reservationService.create(request, authentication);

        assertEquals(pendingResponse, result);

        verify(reservationTransactionService)
                .createPendingReservation(request, user);

        verify(paymentService)
                .validatePayment(any(PaymentValidationRequest.class));
    }

    @Test
    void shouldRejectInvalidDateRange() {
        CreateReservationRequest invalidRequest = new CreateReservationRequest(
                1L,
                LocalDateTime.of(2026, 10, 5, 12, 0),
                LocalDateTime.of(2026, 10, 5, 10, 0),
                PaymentMethod.CREDIT_CARD
        );

        assertThrows(
                InvalidReservationException.class,
                () -> reservationService.create(
                        invalidRequest,
                        authentication
                )
        );
    }

    @Test
    void shouldFindCurrentUserReservations() {
        Reservation first = reservation();
        Reservation second = reservation();

        ReservationResponse firstResponse = response();
        ReservationResponse secondResponse = response();

        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(1L);

        when(reservationRepository.findByUserId(1L))
                .thenReturn(List.of(first, second));

        when(reservationTransactionService.toResponse(first))
                .thenReturn(firstResponse);

        when(reservationTransactionService.toResponse(second))
                .thenReturn(secondResponse);

        List<ReservationResponse> result =
                reservationService.findAll(authentication);

        assertEquals(
                List.of(firstResponse, secondResponse),
                result
        );

        verify(reservationRepository).findByUserId(1L);
    }

    @Test
    void shouldFindAllReservationsForAdmin() {
        Reservation first = reservation();
        Reservation second = reservation();

        ReservationResponse firstResponse = response();
        ReservationResponse secondResponse = response();

        when(reservationRepository.findAllByOrderByStartDateTimeAsc())
                .thenReturn(List.of(first, second));

        when(reservationTransactionService.toResponse(first))
                .thenReturn(firstResponse);

        when(reservationTransactionService.toResponse(second))
                .thenReturn(secondResponse);

        List<ReservationResponse> result =
                reservationService.findAll();

        assertEquals(
                List.of(firstResponse, secondResponse),
                result
        );

        verify(reservationRepository)
                .findAllByOrderByStartDateTimeAsc();
    }

    @Test
    void shouldAllowUserToAccessOwnReservation() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(1L);
        when(user.getRole()).thenReturn(Role.USER);

        when(reservationRepository.findById(10L))
                .thenReturn(Optional.of(reservation));

        when(reservation.getUser()).thenReturn(user);

        ReservationResponse expected = response();

        when(reservationTransactionService.toResponse(reservation))
                .thenReturn(expected);

        ReservationResponse result =
                reservationService.findById(10L, authentication);

        assertEquals(expected, result);
    }

    @Test
    void shouldAllowAdminToAccessAnotherUsersReservation() {
        User reservationOwner = org.mockito.Mockito.mock(User.class);

        when(authentication.getName()).thenReturn("admin@example.com");

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getRole()).thenReturn(Role.ADMIN);

        when(reservationRepository.findById(10L))
                .thenReturn(Optional.of(reservation));

        when(reservation.getUser()).thenReturn(reservationOwner);

        ReservationResponse expected = response();

        when(reservationTransactionService.toResponse(reservation))
                .thenReturn(expected);

        ReservationResponse result =
                reservationService.findById(10L, authentication);

        assertEquals(expected, result);
    }

    @Test
    void shouldRejectUserAccessToAnotherUsersReservation() {
        User reservationOwner = org.mockito.Mockito.mock(User.class);

        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(1L);
        when(user.getRole()).thenReturn(Role.USER);

        when(reservationRepository.findById(10L))
                .thenReturn(Optional.of(reservation));

        when(reservation.getUser()).thenReturn(reservationOwner);
        when(reservationOwner.getId()).thenReturn(2L);

        assertThrows(
                ResourceConflictException.class,
                () -> reservationService.findById(
                        10L,
                        authentication
                )
        );
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(authentication.getName()).thenReturn("unknown@example.com");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.findAll(authentication)
        );
    }

    @Test
    void shouldThrowWhenReservationDoesNotExist() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(reservationRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.findById(
                        10L,
                        authentication
                )
        );
    }

    @Test
    void shouldCancelReservation() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        reservationService.cancel(10L, authentication);

        verify(reservationTransactionService)
                .cancelReservation(10L, user);
    }

    private Reservation reservation() {
        return org.mockito.Mockito.mock(Reservation.class);
    }

    private ReservationResponse response() {
        return new ReservationResponse(
                10L,
                1L,
                1L,
                "Meeting Room A",
                LocalDateTime.of(2026, 10, 5, 10, 0),
                LocalDateTime.of(2026, 10, 5, 12, 0),
                ReservationStatus.CONFIRMED,
                PaymentMethod.CREDIT_CARD
        );
    }
}