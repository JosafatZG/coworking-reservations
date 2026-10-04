package com.coworking.reservations.auth.service;

import com.coworking.reservations.auth.dto.AuthResponse;
import com.coworking.reservations.auth.dto.LoginRequest;
import com.coworking.reservations.auth.dto.RegisterRequest;
import com.coworking.reservations.auth.security.JwtService;
import com.coworking.reservations.common.exception.EmailAlreadyExistsException;
import com.coworking.reservations.common.exception.InvalidCredentialsException;
import com.coworking.reservations.user.entity.Role;
import com.coworking.reservations.user.entity.User;
import com.coworking.reservations.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String BEARER_TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(
                email,
                encodedPassword,
                Role.USER
        );

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                BEARER_TOKEN_TYPE,
                jwtExpiration()
        );
    }

    private long jwtExpiration() {
        return jwtService.getExpiration();
    }
}