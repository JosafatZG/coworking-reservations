package com.coworking.reservations.space.controller;

import com.coworking.reservations.config.security.SecurityAuthorities;
import com.coworking.reservations.space.dto.CreateSpaceRequest;
import com.coworking.reservations.space.dto.SpaceResponse;
import com.coworking.reservations.space.dto.UpdateSpaceRequest;
import com.coworking.reservations.space.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SpaceController {

    private final SpaceService spaceService;

    @Operation(summary = "Create a coworking space")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Space created successfully"),
            @ApiResponse(responseCode = "409", description = "A space with the same name already exists"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(SecurityAuthorities.ADMIN)
    public SpaceResponse create(
            @Valid @RequestBody CreateSpaceRequest request) {

        return spaceService.create(request);
    }

    @Operation(summary = "List all coworking spaces")
    @GetMapping
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public List<SpaceResponse> findAll() {
        return spaceService.findAll();
    }

    @Operation(summary = "Get a coworking space by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Space found"),
            @ApiResponse(responseCode = "404", description = "Space not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize(SecurityAuthorities.USER_OR_ADMIN)
    public SpaceResponse findById(@PathVariable Long id) {
        return spaceService.findById(id);
    }

    @Operation(summary = "Update a coworking space")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Space updated successfully"),
            @ApiResponse(responseCode = "404", description = "Space not found"),
            @ApiResponse(responseCode = "409", description = "A space with the same name already exists"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    @PutMapping("/{id}")
    @PreAuthorize(SecurityAuthorities.ADMIN)
    public SpaceResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSpaceRequest request) {

        return spaceService.update(id, request);
    }

    @Operation(summary = "Delete a coworking space")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Space deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Space not found"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(SecurityAuthorities.ADMIN)
    public void delete(@PathVariable Long id) {
        spaceService.delete(id);
    }
}