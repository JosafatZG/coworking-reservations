package com.coworking.reservations.reservation.controller;

import com.coworking.reservations.config.security.SecurityAuthorities;
import com.coworking.reservations.reservation.dto.CreateReservationRequest;
import com.coworking.reservations.reservation.dto.ReservationResponse;
import com.coworking.reservations.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
            summary = "Create a reservation",
            description = """
                    Creates a reservation for the authenticated user.
                    The reservation is confirmed only after successful payment validation.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation data"),
            @ApiResponse(responseCode = "409", description = "The space is already reserved for the requested time")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public ReservationResponse create(
            @Valid @RequestBody CreateReservationRequest request,
            Authentication authentication
    ) {
        return reservationService.create(request, authentication);
    }

    @Operation(summary = "List the authenticated user's reservations")
    @GetMapping("/my")
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public List<ReservationResponse> findMine(
            Authentication authentication
    ) {
        return reservationService.findAll(authentication);
    }

    @Operation(summary = "Get a reservation by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to access this reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public ReservationResponse findById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return reservationService.findById(id, authentication);
    }

    @Operation(summary = "Cancel a reservation")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reservation cancelled successfully"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to cancel this reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "409", description = "Reservation cannot be cancelled in its current state")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public void cancel(
            @PathVariable Long id,
            Authentication authentication
    ) {
        reservationService.cancel(id, authentication);
    }

    @Operation(summary = "List all reservations")
    @ApiResponse(
            responseCode = "403",
            description = "Admin role required"
    )
    @GetMapping
    @PreAuthorize(SecurityAuthorities.ADMIN)
    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
    }
}