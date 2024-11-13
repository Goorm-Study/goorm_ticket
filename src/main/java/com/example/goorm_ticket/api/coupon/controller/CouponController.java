package com.example.goorm_ticket.api.coupon.controller;

import com.example.goorm_ticket.api.coupon.service.CouponService;
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

    @GetMapping("/{userId}/coupons")
    public List<CouponResponseDto> getUserCoupons(@PathVariable Long userId) {
        return couponService.getUserCoupons(userId);
    }

}
