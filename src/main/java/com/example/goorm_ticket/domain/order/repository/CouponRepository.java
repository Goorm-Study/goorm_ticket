package com.example.goorm_ticket.domain.order.repository;

import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
