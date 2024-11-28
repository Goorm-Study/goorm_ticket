package com.example.goorm_ticket.domain.coupon.repository;

import com.example.goorm_ticket.domain.coupon.entity.CouponCompensateEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponCompensateEventRepository extends JpaRepository<CouponCompensateEvent, Long> {
}
