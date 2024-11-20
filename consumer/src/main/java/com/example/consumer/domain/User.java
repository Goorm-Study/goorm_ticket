package com.example.consumer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCoupon> userCoupons = new ArrayList<>();

//    // 다대다 관계여서 user_id를 키로 사용하는 hashmap은 못쓰고...그냥 임베디드 타입의 리스트로 했습니다
//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(name = "user_coupon", joinColumns = @JoinColumn(name = "user_id"))
//    private List<CouponEmbeddable> coupons = new ArrayList<>();

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

    public void addCoupon(Coupon coupon) {
        UserCoupon userCoupon = UserCoupon.builder()
                .user(this)
                .coupon(coupon)
                .couponName(coupon.getName())
                .build();
        this.userCoupons.add(userCoupon);
    }
}