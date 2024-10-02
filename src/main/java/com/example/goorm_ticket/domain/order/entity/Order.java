package com.example.goorm_ticket.domain.order.entity;

import com.example.goorm_ticket.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime reservationDate;

    @Column(nullable = false)
    private int paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = true)
    private Long couponId;

    @Column(nullable = false)
    private Long eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private Order(LocalDateTime reservationDate, int paymentAmount, OrderStatus orderStatus, Long couponId, Long eventId, User user) {
        this.reservationDate = reservationDate;
        this.paymentAmount = paymentAmount;
        this.orderStatus = orderStatus;
        this.couponId = couponId;
        this.eventId = eventId;
        this.user = user;
    }

    public static Order of(int paymentAmount, OrderStatus orderStatus, Long couponId, Long eventId, User user) {
        return Order.builder()
                .paymentAmount(paymentAmount)
                .orderStatus(orderStatus)
                .couponId(couponId)
                .eventId(eventId)
                .user(user)
                .build();
    }

    public void update(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
