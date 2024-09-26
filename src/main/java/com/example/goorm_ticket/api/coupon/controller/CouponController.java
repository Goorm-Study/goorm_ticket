package com.example.goorm_ticket.api.coupon.controller;

import com.example.goorm_ticket.api.coupon.service.CouponService;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;


    @GetMapping
    public List<CouponResponse> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    @PostMapping("/{user_id}")
    public boolean allocateCouponToUser(@PathVariable Long user_id) {
    }

    @GetMapping("/{id}")
    public List<CouponResponse> getUserCoupons(@PathVariable Long user_id) {

    }

}
