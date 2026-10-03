package com.coworking.reservations.space.repository;

import com.coworking.reservations.space.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    Optional<Space> findByName(String name);
    boolean existsByName(String name);
}