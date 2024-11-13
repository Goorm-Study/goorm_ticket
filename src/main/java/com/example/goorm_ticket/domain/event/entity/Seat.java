package com.example.goorm_ticket.domain.event.entity;

import com.example.goorm_ticket.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long seatNumber;

    @Column(nullable = false)
    private String seatSection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus seatStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Builder
    private Seat(Long seatNumber, String seatSection, SeatStatus seatStatus, Order order, Event event) {
        this.seatNumber = seatNumber;
        this.seatSection = seatSection;
        this.seatStatus = seatStatus;
        this.order = order;
        this.event = event;
    }

    public void update(Order order, SeatStatus seatStatus) {
        this.order = order;
        this.seatStatus = seatStatus;
    }

}
