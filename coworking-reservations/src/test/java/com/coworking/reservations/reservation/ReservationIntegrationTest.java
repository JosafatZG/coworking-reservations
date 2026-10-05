package com.coworking.reservations.reservation;

import com.coworking.reservations.reservation.dto.CreateReservationRequest;
import com.coworking.reservations.reservation.entity.PaymentMethod;
import com.coworking.reservations.reservation.entity.Reservation;
import com.coworking.reservations.reservation.entity.ReservationStatus;
import com.coworking.reservations.reservation.repository.ReservationRepository;
import com.coworking.reservations.space.entity.Space;
import com.coworking.reservations.space.entity.SpaceType;
import com.coworking.reservations.space.repository.SpaceRepository;
import com.coworking.reservations.user.entity.Role;
import com.coworking.reservations.user.entity.User;
import com.coworking.reservations.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "app.security.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                "app.payment.mock.mode=SUCCESS"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ReservationIntegrationTest {

    private static final String USER_EMAIL =
            "reservation-integration@example.com";

    private static final String USER_PASSWORD =
            "User123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private Space space;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();

        userRepository.findByEmail(USER_EMAIL)
                .ifPresent(userRepository::delete);

        user = userRepository.save(
                new User(
                        USER_EMAIL,
                        passwordEncoder.encode(USER_PASSWORD),
                        Role.USER
                )
        );

        space = spaceRepository.save(
                new Space(
                        "Integration Meeting Room",
                        SpaceType.MEETING_ROOM,
                        8,
                        "Integration Location",
                        BigDecimal.valueOf(25)
                )
        );
    }

    @Test
    void shouldCreateAndConfirmReservation() throws Exception {
        String token = login();

        CreateReservationRequest request =
                new CreateReservationRequest(
                        space.getId(),
                        LocalDateTime.of(2026, 11, 10, 10, 0),
                        LocalDateTime.of(2026, 11, 10, 12, 0),
                        PaymentMethod.CREDIT_CARD
                );

        String response = mockMvc.perform(
                        post("/api/reservations")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spaceId")
                        .value(space.getId()))
                .andExpect(jsonPath("$.userId")
                        .value(user.getId()))
                .andExpect(jsonPath("$.spaceName")
                        .value("Integration Meeting Room"))
                .andExpect(jsonPath("$.status")
                        .value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentMethod")
                        .value("CREDIT_CARD"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        Long reservationId =
                json.get("id").asLong();

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                ReservationStatus.CONFIRMED,
                reservation.getStatus()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                user.getId(),
                reservation.getUser().getId()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                space.getId(),
                reservation.getSpace().getId()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                LocalDateTime.of(2026, 11, 10, 10, 0),
                reservation.getStartDateTime()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                LocalDateTime.of(2026, 11, 10, 12, 0),
                reservation.getEndDateTime()
        );
    }

    @Test
    void shouldRejectOverlappingReservation() throws Exception {
        String token = login();

        Reservation existingReservation =
                new Reservation(
                        user,
                        space,
                        LocalDateTime.of(2026, 11, 10, 10, 0),
                        LocalDateTime.of(2026, 11, 10, 12, 0),
                        PaymentMethod.CREDIT_CARD
                );

        existingReservation.confirm();

        reservationRepository.save(existingReservation);

        CreateReservationRequest overlappingRequest =
                new CreateReservationRequest(
                        space.getId(),
                        LocalDateTime.of(2026, 11, 10, 11, 0),
                        LocalDateTime.of(2026, 11, 10, 13, 0),
                        PaymentMethod.CREDIT_CARD
                );

        mockMvc.perform(
                        post("/api/reservations")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                overlappingRequest
                                        )
                                )
                )
                .andExpect(status().isConflict());
    }

    private String login() throws Exception {
        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(
                                        USER_EMAIL,
                                        USER_PASSWORD
                                ))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("token").asText();
    }
}