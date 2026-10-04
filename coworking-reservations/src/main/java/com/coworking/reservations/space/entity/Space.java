package com.coworking.reservations.space.entity;

import com.coworking.reservations.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "spaces")
public class Space extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SpaceType type;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    public Space(
            String name,
            SpaceType type,
            Integer capacity,
            String location,
            BigDecimal hourlyRate) {
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.location = location;
        this.hourlyRate = hourlyRate;
    }

    public void update(
            String name,
            SpaceType type,
            Integer capacity,
            String location,
            BigDecimal hourlyRate) {

        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.location = location;
        this.hourlyRate = hourlyRate;
    }
}