package com.example.goorm_ticket.api.coupon.controller;

import com.example.goorm_ticket.api.coupon.service.CouponService;
import com.example.goorm_ticket.domain.coupon.dto.CouponRequestDto;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public List<CouponResponseDto> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    @PostMapping("/{userId}/{couponId}")
    public CouponResponseDto allocateCouponToUser(@PathVariable Long userId, @PathVariable Long couponId) {
        return couponService.allocateCouponToUser(userId, couponId);
    }

    /*유저의 쿠폰을 조회하는 api는 UserController로 옮기는게 낫지 않을까*/
    @GetMapping("/{userId}/coupons")
    public List<CouponResponseDto> getUserCoupons(@PathVariable Long userId) {
        return couponService.getUserCoupons(userId);
    }

}
