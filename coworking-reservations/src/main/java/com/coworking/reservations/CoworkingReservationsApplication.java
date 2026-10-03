package com.coworking.reservations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CoworkingReservationsApplication {
    static void main(String[] args) {
        SpringApplication.run(CoworkingReservationsApplication.class, args);
    }
}
