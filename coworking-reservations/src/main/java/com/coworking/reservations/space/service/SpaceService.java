package com.coworking.reservations.space.service;

import com.coworking.reservations.common.exception.ResourceConflictException;
import com.coworking.reservations.common.exception.ResourceNotFoundException;
import com.coworking.reservations.space.dto.CreateSpaceRequest;
import com.coworking.reservations.space.dto.SpaceResponse;
import com.coworking.reservations.space.dto.UpdateSpaceRequest;
import com.coworking.reservations.space.entity.Space;
import com.coworking.reservations.space.mapper.SpaceMapper;
import com.coworking.reservations.space.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final SpaceMapper spaceMapper;

    @CacheEvict(cacheNames = "occupancyReports", allEntries = true)
    @Transactional
    public SpaceResponse create(CreateSpaceRequest request) {

        if (spaceRepository.existsByName(request.name())) {
            throw new ResourceConflictException(
                    "A space with name '%s' already exists".formatted(request.name()));
        }

        Space space = spaceMapper.toEntity(request);

        return spaceMapper.toResponse(spaceRepository.save(space));
    }

    public List<SpaceResponse> findAll() {
        return spaceRepository.findAll()
                .stream()
                .map(spaceMapper::toResponse)
                .toList();
    }

    public SpaceResponse findById(Long id) {
        return spaceMapper.toResponse(findEntityById(id));
    }

    @CacheEvict(cacheNames = "occupancyReports", allEntries = true)
    @Transactional
    public SpaceResponse update(Long id, UpdateSpaceRequest request) {

        Space space = findEntityById(id);

        if (!space.getName().equals(request.name())
                && spaceRepository.existsByName(request.name())) {
            throw new ResourceConflictException(
                    "A space with name '%s' already exists".formatted(request.name()));
        }

        space.update(
                request.name(),
                request.type(),
                request.capacity(),
                request.location(),
                request.hourlyRate());

        return spaceMapper.toResponse(space);
    }

    @CacheEvict(cacheNames = "occupancyReports", allEntries = true)
    @Transactional
    public void delete(Long id) {
        Space space = findEntityById(id);
        spaceRepository.delete(space);
    }

    private Space findEntityById(Long id) {
        return spaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Space with id %d not found".formatted(id)));
    }
}