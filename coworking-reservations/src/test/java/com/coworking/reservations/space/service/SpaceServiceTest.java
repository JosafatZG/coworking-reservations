package com.coworking.reservations.space.service;

import com.coworking.reservations.common.exception.ResourceConflictException;
import com.coworking.reservations.common.exception.ResourceNotFoundException;
import com.coworking.reservations.space.dto.CreateSpaceRequest;
import com.coworking.reservations.space.dto.SpaceResponse;
import com.coworking.reservations.space.dto.UpdateSpaceRequest;
import com.coworking.reservations.space.entity.Space;
import com.coworking.reservations.space.mapper.SpaceMapper;
import com.coworking.reservations.space.repository.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private SpaceMapper spaceMapper;

    @Mock
    private CreateSpaceRequest createRequest;

    @Mock
    private UpdateSpaceRequest updateRequest;

    @Mock
    private Space space;

    @Mock
    private Space savedSpace;

    @Mock
    private SpaceResponse response;

    private SpaceService spaceService;

    @BeforeEach
    void setUp() {
        spaceService = new SpaceService(
                spaceRepository,
                spaceMapper
        );
    }

    @Test
    void shouldCreateSpace() {
        when(createRequest.name())
                .thenReturn("Sala Ejecutiva");

        when(spaceRepository.existsByName("Sala Ejecutiva"))
                .thenReturn(false);

        when(spaceMapper.toEntity(createRequest))
                .thenReturn(space);

        when(spaceRepository.save(space))
                .thenReturn(savedSpace);

        when(spaceMapper.toResponse(savedSpace))
                .thenReturn(response);

        SpaceResponse result =
                spaceService.create(createRequest);

        assertThat(result)
                .isSameAs(response);

        verify(spaceRepository)
                .existsByName("Sala Ejecutiva");

        verify(spaceMapper)
                .toEntity(createRequest);

        verify(spaceRepository)
                .save(space);

        verify(spaceMapper)
                .toResponse(savedSpace);
    }

    @Test
    void shouldRejectDuplicateSpaceName() {
        when(createRequest.name())
                .thenReturn("Sala Ejecutiva");

        when(spaceRepository.existsByName("Sala Ejecutiva"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                spaceService.create(createRequest)
        )
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("A space with name 'Sala Ejecutiva' already exists");

        verify(spaceRepository, never())
                .save(any());

        verify(spaceMapper, never())
                .toEntity(any());
    }

    @Test
    void shouldFindAllSpaces() {
        Space anotherSpace = org.mockito.Mockito.mock(Space.class);
        SpaceResponse anotherResponse = org.mockito.Mockito.mock(SpaceResponse.class);

        when(spaceRepository.findAll())
                .thenReturn(List.of(space, anotherSpace));

        when(spaceMapper.toResponse(space))
                .thenReturn(response);

        when(spaceMapper.toResponse(anotherSpace))
                .thenReturn(anotherResponse);

        List<SpaceResponse> result =
                spaceService.findAll();

        assertThat(result)
                .containsExactly(response, anotherResponse);

        verify(spaceMapper)
                .toResponse(space);

        verify(spaceMapper)
                .toResponse(anotherSpace);
    }

    @Test
    void shouldFindSpaceById() {
        when(spaceRepository.findById(1L))
                .thenReturn(Optional.of(space));

        when(spaceMapper.toResponse(space))
                .thenReturn(response);

        SpaceResponse result =
                spaceService.findById(1L);

        assertThat(result)
                .isSameAs(response);

        verify(spaceRepository)
                .findById(1L);

        verify(spaceMapper)
                .toResponse(space);
    }

    @Test
    void shouldRejectFindByIdWhenSpaceDoesNotExist() {
        when(spaceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                spaceService.findById(1L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Space with id 1 not found");
    }

    @Test
    void shouldUpdateSpace() {
        when(spaceRepository.findById(1L))
                .thenReturn(Optional.of(space));

        when(space.getName())
                .thenReturn("Old Name");

        when(updateRequest.name())
                .thenReturn("New Name");

        when(spaceRepository.existsByName("New Name"))
                .thenReturn(false);

        when(spaceMapper.toResponse(space))
                .thenReturn(response);

        SpaceResponse result =
                spaceService.update(1L, updateRequest);

        assertThat(result)
                .isSameAs(response);

        verify(space)
                .update(
                        updateRequest.name(),
                        updateRequest.type(),
                        updateRequest.capacity(),
                        updateRequest.location(),
                        updateRequest.hourlyRate()
                );
    }

    @Test
    void shouldRejectUpdateWhenNewNameAlreadyExists() {
        when(spaceRepository.findById(1L))
                .thenReturn(Optional.of(space));

        when(space.getName())
                .thenReturn("Old Name");

        when(updateRequest.name())
                .thenReturn("Existing Name");

        when(spaceRepository.existsByName("Existing Name"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                spaceService.update(1L, updateRequest)
        )
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("A space with name 'Existing Name' already exists");

        verify(space, never())
                .update(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldNotCheckDuplicateNameWhenNameDoesNotChange() {
        when(spaceRepository.findById(1L))
                .thenReturn(Optional.of(space));

        when(space.getName())
                .thenReturn("Sala Ejecutiva");

        when(updateRequest.name())
                .thenReturn("Sala Ejecutiva");

        when(spaceMapper.toResponse(space))
                .thenReturn(response);

        SpaceResponse result =
                spaceService.update(1L, updateRequest);

        assertThat(result)
                .isSameAs(response);

        verify(spaceRepository, never())
                .existsByName(any());

        verify(space)
                .update(
                        updateRequest.name(),
                        updateRequest.type(),
                        updateRequest.capacity(),
                        updateRequest.location(),
                        updateRequest.hourlyRate()
                );
    }

    @Test
    void shouldDeleteSpace() {
        when(spaceRepository.findById(1L))
                .thenReturn(Optional.of(space));

        spaceService.delete(1L);

        verify(spaceRepository)
                .delete(space);
    }

    @Test
    void shouldRejectDeleteWhenSpaceDoesNotExist() {
        when(spaceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                spaceService.delete(1L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Space with id 1 not found");

        verify(spaceRepository, never())
                .delete(any());
    }
}