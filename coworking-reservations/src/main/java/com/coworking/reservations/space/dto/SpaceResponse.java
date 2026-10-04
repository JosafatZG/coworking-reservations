package com.coworking.reservations.space.dto;

import com.coworking.reservations.space.entity.SpaceType;

import java.math.BigDecimal;

public record SpaceResponse(
        Long id,
        String name,
        SpaceType type,
        Integer capacity,
        String location,
        BigDecimal hourlyRate
) {
}