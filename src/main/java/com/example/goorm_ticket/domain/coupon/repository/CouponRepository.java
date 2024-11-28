package com.example.goorm_ticket.domain.coupon.repository;

import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select c from Coupon c where c.id = :id")
    Optional<Coupon> findByIdWithPessimisticLock(@Param("id") Long id);

    // TODO: QueryDsl로 바꿔보기
    @Query(value = "SELECT EXISTS (" +
            "  SELECT 1 FROM user_coupon " +
            "  WHERE user_id = :userId AND coupon_id = :couponId" +
            ")", nativeQuery = true)
    Long existsByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);
}
