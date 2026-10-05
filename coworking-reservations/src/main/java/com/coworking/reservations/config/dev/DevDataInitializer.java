package com.coworking.reservations.config.dev;

import com.coworking.reservations.space.entity.Space;
import com.coworking.reservations.space.entity.SpaceType;
import com.coworking.reservations.space.repository.SpaceRepository;
import com.coworking.reservations.user.entity.Role;
import com.coworking.reservations.user.entity.User;
import com.coworking.reservations.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        initializeUsers();
        initializeSpaces();
    }

    private void initializeUsers() {
        createUserIfNotExists(
                "admin@localhost",
                "Admin123!",
                Role.ADMIN
        );

        createUserIfNotExists(
                "user1@localhost",
                "User123!",
                Role.USER
        );

        createUserIfNotExists(
                "user2@localhost",
                "User123!",
                Role.USER
        );
    }

    private void initializeSpaces() {
        createSpaceIfNotExists(
                "Sala Ejecutiva",
                SpaceType.MEETING_ROOM,
                10,
                "Primer piso",
                new BigDecimal("25.00")
        );

        createSpaceIfNotExists(
                "Sala Creativa",
                SpaceType.MEETING_ROOM,
                6,
                "Primer piso",
                new BigDecimal("18.00")
        );

        createSpaceIfNotExists(
                "Workstation A",
                SpaceType.WORKSTATION,
                1,
                "Segundo piso",
                new BigDecimal("8.00")
        );

        createSpaceIfNotExists(
                "Workstation B",
                SpaceType.WORKSTATION,
                1,
                "Segundo piso",
                new BigDecimal("8.00")
        );
    }

    private void createUserIfNotExists(
            String email,
            String password,
            Role role
    ) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        userRepository.save(
                new User(
                        email,
                        passwordEncoder.encode(password),
                        role
                )
        );
    }

    private void createSpaceIfNotExists(
            String name,
            SpaceType type,
            int capacity,
            String location,
            BigDecimal hourlyRate
    ) {
        if (spaceRepository.existsByName(name)) {
            return;
        }

        spaceRepository.save(
                new Space(
                        name,
                        type,
                        capacity,
                        location,
                        hourlyRate
                )
        );
    }
}