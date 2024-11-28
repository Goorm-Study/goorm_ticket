package com.example.goorm_ticket.domain.user.entity;

import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_coupon",
            joinColumns = @JoinColumn(name = "user_id"),
            // 한 유저는 같은 쿠폰을 중복해서 발급받을 수 없음
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "coupon_id"})
    )
    private List<CouponEmbeddable> coupons = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Order> order = new ArrayList<>();

    @Builder
    private User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static User of(String username, String password) {
        return User.builder()
                .username(username)
                .password(password)
                .build();
    }
}