package com.coworking.reservations.space.controller;

import com.coworking.reservations.config.security.SecurityAuthorities;
import com.coworking.reservations.space.dto.CreateSpaceRequest;
import com.coworking.reservations.space.dto.SpaceResponse;
import com.coworking.reservations.space.dto.UpdateSpaceRequest;
import com.coworking.reservations.space.service.SpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(SecurityAuthorities.ADMIN)
    public SpaceResponse create(
            @Valid @RequestBody CreateSpaceRequest request) {

        return spaceService.create(request);
    }

    @GetMapping
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public List<SpaceResponse> findAll() {
        return spaceService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public SpaceResponse findById(@PathVariable Long id) {
        return spaceService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityAuthorities.ADMIN)
    public SpaceResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSpaceRequest request) {

        return spaceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(SecurityAuthorities.ADMIN)
    public void delete(@PathVariable Long id) {
        spaceService.delete(id);
    }
}