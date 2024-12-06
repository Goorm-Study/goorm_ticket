package com.example.shared.domain.order.entity;

import com.example.shared.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
