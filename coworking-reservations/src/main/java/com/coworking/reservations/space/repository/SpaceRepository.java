package com.coworking.reservations.space.repository;

import com.coworking.reservations.space.entity.Space;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    Optional<Space> findByName(String name);
    boolean existsByName(String name);
    List<Space> findAllByOrderByNameAsc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT s
    FROM Space s
    WHERE s.id = :id
    """)
    Optional<Space> findByIdForUpdate(@Param("id") Long id);
}