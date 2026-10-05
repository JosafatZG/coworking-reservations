package com.coworking.reservations.auth;

import com.coworking.reservations.space.dto.CreateSpaceRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AuthenticationIntegrationTest {

    private static final String USER_EMAIL = "integration-user@example.com";
    private static final String USER_PASSWORD = "User123!";

    private static final String ADMIN_EMAIL = "integration-admin@example.com";
    private static final String ADMIN_PASSWORD = "Admin123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SpaceRepository spaceRepository;

    @BeforeEach
    void setUp() {
        spaceRepository.deleteAll();

        userRepository.findByEmail(USER_EMAIL)
                .ifPresent(userRepository::delete);

        userRepository.findByEmail(ADMIN_EMAIL)
                .ifPresent(userRepository::delete);

        userRepository.save(
                new User(
                        USER_EMAIL,
                        passwordEncoder.encode(USER_PASSWORD),
                        Role.USER
                )
        );

        userRepository.save(
                new User(
                        ADMIN_EMAIL,
                        passwordEncoder.encode(ADMIN_PASSWORD),
                        Role.ADMIN
                )
        );
    }

    @Test
    void shouldAuthenticateUserAndAccessProtectedEndpoint() throws Exception {
        String token = login(USER_EMAIL, USER_PASSWORD);

        mockMvc.perform(
                        get("/api/spaces")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldRejectUserFromAdminOnlyEndpoint() throws Exception {
        String token = login(USER_EMAIL, USER_PASSWORD);

        CreateSpaceRequest request = new CreateSpaceRequest(
                "Integration Space",
                SpaceType.MEETING_ROOM,
                6,
                "Integration Location",
                BigDecimal.valueOf(20)
        );

        mockMvc.perform(
                        post("/api/spaces")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToCreateSpace() throws Exception {
        String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);

        CreateSpaceRequest request = new CreateSpaceRequest(
                "Integration Admin Space",
                SpaceType.MEETING_ROOM,
                8,
                "Integration Location",
                BigDecimal.valueOf(25)
        );

        mockMvc.perform(
                        post("/api/spaces")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name")
                        .value("Integration Admin Space"))
                .andExpect(jsonPath("$.capacity")
                        .value(8));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(
                        get("/api/spaces")
                )
                .andExpect(status().isUnauthorized());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(email, password))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("token").asText();
    }
}