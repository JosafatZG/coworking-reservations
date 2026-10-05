package com.coworking.reservations.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.payment.mock")
public record PaymentMockProperties(
        Mode mode,
        Duration delay
) {

    public PaymentMockProperties {
        mode = mode == null ? Mode.SUCCESS : mode;
        delay = delay == null ? Duration.ZERO : delay;
    }

    public enum Mode {
        SUCCESS,
        FAILURE,
        SLOW
    }
}