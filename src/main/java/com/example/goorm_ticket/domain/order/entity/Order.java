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
    private Long orderID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private Order(LocalDateTime reservationDate, int paymentAmount, OrderStatus orderStatus, Long couponId, Long orderID, User user) {
        this.reservationDate = reservationDate;
        this.paymentAmount = paymentAmount;
        this.orderStatus = orderStatus;
        this.couponId = couponId;
        this.orderID = orderID;
        this.user = user;
    }

    public static Order createOrder(int paymentAmount, OrderStatus orderStatus, Long couponId, Long orderID, User user) {
        return Order.builder()
                .paymentAmount(paymentAmount)
                .orderStatus(orderStatus)
                .couponId(couponId)
                .orderID(orderID)
                .user(user)
                .build();
    }

}
