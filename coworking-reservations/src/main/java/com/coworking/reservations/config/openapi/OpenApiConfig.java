package com.coworking.reservations.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI coworkingReservationsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Coworking Reservations API")
                        .description("""
                                REST API for managing coworking spaces and reservations.

                                The API supports user authentication, space management,
                                reservations, payment validation, and occupancy reports.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Coworking Reservations API")
                        ))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}