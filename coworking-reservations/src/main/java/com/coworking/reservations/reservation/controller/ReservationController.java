package com.coworking.reservations.reservation.controller;

import com.coworking.reservations.config.security.SecurityAuthorities;
import com.coworking.reservations.reservation.dto.CreateReservationRequest;
import com.coworking.reservations.reservation.dto.ReservationResponse;
import com.coworking.reservations.reservation.service.ReservationService;
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
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public ReservationResponse create(
            @Valid @RequestBody CreateReservationRequest request,
            Authentication authentication
    ) {
        return reservationService.create(request, authentication);
    }

    @GetMapping("/my")
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public List<ReservationResponse> findMine(
            Authentication authentication
    ) {
        return reservationService.findAll(authentication);
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public ReservationResponse findById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return reservationService.findById(id, authentication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public void cancel(
            @PathVariable Long id,
            Authentication authentication
    ) {
        reservationService.cancel(id, authentication);
    }

    @GetMapping
    @PreAuthorize(SecurityAuthorities.ADMIN)
    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
    }
}