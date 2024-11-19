package com.example.goorm_ticket.api.coupon.controller;

import com.example.goorm_ticket.api.coupon.service.CouponAllocationService;
import com.example.goorm_ticket.api.coupon.service.CouponQueryService;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponAllocationService couponAllocationService;
    private final CouponQueryService couponQueryService;

    @GetMapping
    public List<CouponResponseDto> getAllCoupons() {
        return couponQueryService.getAllCoupons();
    }

    @PostMapping("/{userId}/{couponId}")
    public CouponResponseDto allocateCouponToUser(@PathVariable Long userId, @PathVariable Long couponId) {
        return couponAllocationService.allocateCouponToUser(userId, couponId);
    }

    //동시성 제어 X
//    @PostMapping("/{userId}/{couponId}")
//    public CouponResponseDto allocateCouponToUser(@PathVariable Long userId, @PathVariable Long couponId) {
//        return couponService.allocateCouponToUser(userId, couponId);
//    }

    //비관적 락 동시성 제어
//    @PostMapping("/{userId}/{couponId}")
//    public CouponResponseDto allocateCouponToUser(@PathVariable Long userId, @PathVariable Long couponId) {
//        return couponService.allocateCouponToUserWithPessimisticLock(userId, couponId);
//    }

//    //낙관적 락 동시성 제어
//    @PostMapping("/{userId}/{couponId}")
//    public CouponResponseDto allocateCouponToUser(@PathVariable Long userId, @PathVariable Long couponId) {
//        return couponService.allocateCouponToUserWithOptimisticLock(userId, couponId);
//    }

//    // 분산 락 동시성 제어
//    @PostMapping("/{userId}/{couponId}")
//    public CouponResponseDto allocateCouponToUser(@PathVariable Long userId, @PathVariable Long couponId) {
//        return couponAllocationService.allocateCouponToUserWithRedis(userId, couponId);
//    }



    /*유저의 쿠폰을 조회하는 api는 UserController로 옮기는게 낫지 않을까*/
    @GetMapping("/{userId}/coupons")
    public List<CouponResponseDto> getUserCoupons(@PathVariable Long userId) {
        return couponQueryService.getUserCoupons(userId);
    }

}
