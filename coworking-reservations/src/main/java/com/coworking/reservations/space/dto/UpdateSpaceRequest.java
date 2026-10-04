package com.coworking.reservations.space.dto;

import com.coworking.reservations.space.entity.SpaceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateSpaceRequest(

        @NotBlank
        String name,

        @NotNull
        SpaceType type,

        @NotNull
        @Min(1)
        Integer capacity,

        @NotBlank
        String location,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal hourlyRate
) {
}