package com.coworking.reservations.report.dto;

import com.coworking.reservations.space.entity.SpaceType;

import java.math.BigDecimal;

public record OccupancyReportResponse(
        Long spaceId,
        String spaceName,
        SpaceType spaceType,
        BigDecimal occupancyPercentage
) {
}