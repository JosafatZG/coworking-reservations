package com.coworking.reservations.report.controller;

import com.coworking.reservations.config.security.SecurityAuthorities;
import com.coworking.reservations.report.dto.OccupancyReportResponse;
import com.coworking.reservations.report.service.OccupancyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OccupancyReportController {

    private final OccupancyReportService occupancyReportService;

    @Operation(
            summary = "Generate occupancy report",
            description = """
                    Returns the occupancy percentage for each coworking space
                    within the specified date-time range.
                    Requires ADMIN role.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Occupancy report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date-time range"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    @GetMapping("/occupancy")
    @PreAuthorize(SecurityAuthorities.ADMIN)
    public List<OccupancyReportResponse> getOccupancyReport(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {
        return occupancyReportService.generateReport(start, end);
    }
}