package com.coworking.reservations.space.mapper;

import com.coworking.reservations.space.dto.CreateSpaceRequest;
import com.coworking.reservations.space.dto.SpaceResponse;
import com.coworking.reservations.space.entity.Space;
import org.springframework.stereotype.Component;

@Component
public class SpaceMapper {

    public Space toEntity(CreateSpaceRequest request) {
        return new Space(
                request.name(),
                request.type(),
                request.capacity(),
                request.location(),
                request.hourlyRate());
    }

    public SpaceResponse toResponse(Space space) {
        return new SpaceResponse(
                space.getId(),
                space.getName(),
                space.getType(),
                space.getCapacity(),
                space.getLocation(),
                space.getHourlyRate());
    }
}