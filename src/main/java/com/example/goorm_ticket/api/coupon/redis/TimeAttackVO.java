package com.example.goorm_ticket.api.coupon.redis;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeAttackVO {
    private Long userId;        // 사용자 ID
    private Long policyId;      // 쿠폰 정책 ID

    // Redis에서 사용할 키 생성 메서드 (eventTimeId 제거)
    public String getKey() {
        return "coupon:time-attack:" + policyId + ":issued:users";
    }
}