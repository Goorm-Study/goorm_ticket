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

    // 다대다 관계여서 user_id를 키로 사용하는 hashmap은 못쓰고...그냥 임베디드 타입의 리스트로 했습니다
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_coupon", joinColumns = @JoinColumn(name = "user_id"))
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