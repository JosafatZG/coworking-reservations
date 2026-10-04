package com.coworking.reservations.config.dev;

import com.coworking.reservations.user.entity.Role;
import com.coworking.reservations.user.entity.User;
import com.coworking.reservations.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {

        if (userRepository.existsByEmail("admin@localhost")) {
            return;
        }

        userRepository.save(
                new User(
                        "admin@localhost",
                        passwordEncoder.encode("Admin123!"),
                        Role.ADMIN
                )
        );
    }
}