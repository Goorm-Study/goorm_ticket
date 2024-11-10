package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.aop.annotation.DistributedLock;
import com.example.goorm_ticket.aop.annotation.NamedLock;
import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.goorm_ticket.api.coupon.exception.CouponException.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_COUPON_PREFIX = "COUPON:";

    @Transactional(readOnly = true)
    public List<CouponResponseDto> getAllCoupons() {
        List<Coupon> couponList = couponRepository.findAll();
        return couponList.stream()
                .map(coupon -> CouponResponseDto.of(
                        coupon.getId(),
                        coupon.getQuantity(),
                        coupon.getName(),
                        coupon.getDiscountRate(),
                        coupon.getExpirationDate())
                )
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CouponResponseDto> getUserCoupons(Long userId) {
        User user = findUserById(userId);
        return user.getCoupons().stream()
                .map(coupon -> CouponResponseDto.of(coupon.getId(), coupon.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CouponResponseDto decreaseCoupon(Long couponId) {
        Coupon coupon = findCouponById(couponId);
        coupon.decreaseQuantity(1L);

        return CouponResponseDto.of(coupon.getId(), coupon.getName());
    }

    public CouponResponseDto decreaseCouponFromRedis(Long couponId, Long quantity) {
        String key = getRedisCouponKey(couponId);
        Long remaining = getCouponQuantityFromRedis(couponId, key);
//        log.info("remaining: {}", remaining);
        if(remaining < quantity) {
            throw new CouponQuantityShortageException(remaining, quantity);
        }

        stringRedisTemplate.opsForHash().increment(key, "quantity", -quantity);

        return CouponResponseDto.of(couponId);
    }

    private Long getCouponQuantityFromRedis(Long couponId, String key) {
        String value = (String) stringRedisTemplate.opsForHash().get(key, "quantity");
        if(value == null) { // redis에 캐시되어있지 않을 경우 캐시하기
            Coupon coupon = cacheCouponToRedis(key, couponId);
            return coupon.getQuantity();
        }
        return Long.valueOf(value);
    }

    @Transactional
    public CouponResponseDto decreaseCouponWithPessimisticLock(Long couponId) {
        Coupon coupon = findCouponByIdWithPessimisticLock(couponId);
        coupon.decreaseQuantity(1L);

        return CouponResponseDto.of(coupon.getId(), coupon.getName());
    }

    @Transactional
    public CouponResponseDto allocateCouponToUserWithPessimisticLock(Long userId, Long couponId) {
        User user = findUserById(userId);

        CouponResponseDto couponResponseDto = decreaseCouponWithPessimisticLock(couponId);
        addCouponToUserCoupons(user, couponResponseDto);

        return couponResponseDto;
    }

    @NamedLock(lockKey = "'coupon' + #couponId")
    public CouponResponseDto allocateCouponToUserWithNamedLock(Long userId, Long couponId) {
        User user = findUserById(userId);

        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);
        addCouponToUserCoupons(user, couponResponseDto);

        return couponResponseDto;
    }

    @DistributedLock(key = "'coupon' + #couponId")
    public CouponResponseDto allocateCouponToUserWithDistributedLock(Long userId, Long couponId) {
        User user = findUserById(userId);

        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);
        addCouponToUserCoupons(user, couponResponseDto);

        return couponResponseDto;
    }

    @DistributedLock(key = "'coupon' + #couponId")
    public CouponResponseDto allocateRedisCouponToUserWithDistributedLock(Long userId, Long couponId) {
//        User user = findUserById(userId);

//        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);
//        addCouponToUserCoupons(user, couponResponseDto);
        CouponResponseDto couponResponseDto = decreaseCouponFromRedis(couponId, 1L);
        // TODO: 메시지큐에 쿠폰 발급 이벤트 발행

        return couponResponseDto;
    }

    private static void addCouponToUserCoupons(User user, CouponResponseDto couponResponseDto) {
        List<CouponEmbeddable> userCoupons = user.getCoupons();
        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId).orElseThrow(() -> new CouponNotFoundException(couponId));
    }

    public Coupon findCouponByIdWithPessimisticLock(Long couponId) {
        return couponRepository.findByIdWithPessimisticLock(couponId).orElseThrow(() -> new CouponException.CouponNotFoundException(couponId));
    }

    private static String getRedisCouponKey(Long couponId) {
        return REDIS_COUPON_PREFIX + couponId;
    }

    private Coupon cacheCouponToRedis(String key, Long couponId) {
        Coupon coupon = findCouponById(couponId);
        stringRedisTemplate.opsForHash().put(key, "couponId", couponId.toString());
        stringRedisTemplate.opsForHash().put(key, "quantity", coupon.getQuantity().toString());
        return coupon;
    }
}
