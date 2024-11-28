package com.example.goorm_ticket.config;

public class RedisCouponConstants {
    public static final String REDIS_COUPON_PREFIX = "COUPON:";
    public static final String REDIS_PENDING_EVENT_KEY = "PENDING_EVENT";

    public static String getRedisCouponKey(Long couponId) {
        return RedisCouponConstants.REDIS_COUPON_PREFIX + couponId;
    }
}
