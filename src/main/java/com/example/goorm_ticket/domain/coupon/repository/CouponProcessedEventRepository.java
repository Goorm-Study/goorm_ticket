package com.example.goorm_ticket.domain.coupon.repository;

import com.example.goorm_ticket.domain.coupon.entity.CouponProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponProcessedEventRepository extends JpaRepository<CouponProcessedEvent, Long> {
    Boolean existsByEventId(Long eventId); // TODO: 이거 성능 이슈 있어서 QueryDsl로 바꾸기
}
