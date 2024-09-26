package com.example.goorm_ticket.api.coupon.controller;

import com.example.goorm_ticket.api.coupon.service.CouponService;
import com.example.goorm_ticket.api.coupon.service.UserCouponService;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;
    private final UserCouponService userCouponService;


    @GetMapping
    public List<CouponResponse> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    @PostMapping("/{user_id}/{coupon_id}")
    public CouponResponse allocateCouponToUser(@PathVariable(name="user_id") Long user_id,
                                        @PathVariable(name="coupon_id") Long coupon_id) {
        return userCouponService.allocateCouponToUser(user_id, coupon_id);
    }

    // 이름말고 CouponResponse 반환하도록 하는게 나을듯
    @GetMapping("/{id}")
    public List<CouponResponse> getUserCoupons(@PathVariable Long user_id) {
        return couponService.getUserCoupons(user_id);
    }

}
