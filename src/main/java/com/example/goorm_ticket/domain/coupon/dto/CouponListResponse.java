package com.example.goorm_ticket.domain.coupon.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CouponListResponse {

    private List<CouponResponse> coupons;
}
