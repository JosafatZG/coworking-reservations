package com.coworking.reservations.reservation.entity;

import com.coworking.reservations.common.audit.Auditable;
import com.coworking.reservations.space.entity.Space;
import com.coworking.reservations.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "reservations")
public class Reservation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    public Reservation(
            User user,
            Space space,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            PaymentMethod paymentMethod) {
        this.user = user;
        this.space = space;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.paymentMethod = paymentMethod;
        this.status = ReservationStatus.PENDING_PAYMENT;
    }
}