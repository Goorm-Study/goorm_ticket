package com.example.goorm_ticket.domain.order.entity;

import com.example.goorm_ticket.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime reservationDate;

    @Column(nullable = false)
    private Long paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = true)
    private Long couponId;

    @Column(nullable = false)
    private Long orderID; // Event ID를 나타내는 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 주문 상태를 Enum으로 처리
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Order(LocalDateTime reservationDate, Long paymentAmount, OrderStatus orderStatus, Long couponId, Long orderID) {
        this.reservationDate = reservationDate;
        this.paymentAmount = paymentAmount;
        this.orderStatus = orderStatus;
        this.couponId = couponId;
        this.orderID = orderID;
    }

}
