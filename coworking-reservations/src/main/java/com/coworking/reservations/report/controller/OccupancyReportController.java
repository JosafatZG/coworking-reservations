package com.coworking.reservations.report.controller;

import com.coworking.reservations.config.security.SecurityAuthorities;
import com.coworking.reservations.report.dto.OccupancyReportResponse;
import com.coworking.reservations.report.service.OccupancyReportService;
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
public class OccupancyReportController {

    private final OccupancyReportService occupancyReportService;

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