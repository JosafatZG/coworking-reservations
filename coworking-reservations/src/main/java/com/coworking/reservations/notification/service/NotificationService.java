package com.coworking.reservations.notification.service;

import com.coworking.reservations.reservation.event.ReservationConfirmedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class NotificationService {

    @Async("notificationExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleReservationConfirmed(
            ReservationConfirmedEvent event
    ) {
        log.info(
                "Simulated email sent to {} for confirmed reservation {} " +
                        "at space '{}'",
                event.userEmail(),
                event.reservationId(),
                event.spaceName()
        );
    }
}