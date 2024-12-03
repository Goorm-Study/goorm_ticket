package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.aop.annotation.DistributedLock;
import com.example.goorm_ticket.aop.annotation.NamedLock;
import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponEventDto;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import com.example.goorm_ticket.kafka.CouponKafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.goorm_ticket.config.RedisCouponConstants;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.example.goorm_ticket.api.coupon.exception.CouponException.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final CouponKafkaProducer couponKafkaProducer;
    private final ObjectMapper objectMapper;
    private final RedisScript<Long> decrAndSaveMessageScript;

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
                .map(coupon -> CouponResponseDto.of(coupon.getCouponId(), coupon.getCouponName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Boolean isUserCouponAllocated(Long userId, Long couponId) {
        Long isExist = couponRepository.existsByUserIdAndCouponId(userId, couponId);
        if(isExist != 0L) {
            log.warn("user: {}는 쿠폰이 이미 발급된 유저입니다.", userId);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public CouponResponseDto decreaseCoupon(Long couponId) {
        Coupon coupon = findCouponById(couponId);
        coupon.decreaseQuantity(1L);

        return CouponResponseDto.of(coupon.getId(), coupon.getName());
    }

    public CouponEventDto decreaseCouponFromRedis(Long userId, Long couponId, Long quantity) {
        // 발생할 수 있는 예외 1. 쿠폰id에 해당하는 쿠폰이 없음, 2. 쿠폰 수량 부족
        // 정상적으로 실행 될 시 eventId 반환
        Long result = stringRedisTemplate.execute(decrAndSaveMessageScript,
                List.of(RedisCouponConstants.getRedisCouponKey(couponId)),
                couponId.toString(),
                userId.toString());
        return switch (result.intValue()) {
            case -1 -> throw new CouponNotFoundException(couponId);
            case -2 -> throw new CouponQuantityShortageException(0L, 1L);
            default -> CouponEventDto.of(userId, couponId, result);
        };
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

    public CouponResponseDto allocateRedisCouponToUser(Long userId, Long couponId) {
        CouponEventDto couponEventDto = decreaseCouponFromRedis(userId, couponId, 1L);
        if (couponEventDto == null) {
            return null;
        }
        // 만약 여기서 애플리케이션 서버가 다운된다면?
        publishCouponEvent(couponEventDto);
        return CouponResponseDto.of(couponId);
    }

    private void publishCouponEvent(CouponEventDto couponEventDto) {
        CompletableFuture<SendResult<String, CouponEventDto>> future = couponKafkaProducer.publishEvent(couponEventDto);
        future.whenComplete((result, ex) -> {
            Long eventId = result.getProducerRecord().value().getEventId();
            removePendingEvent(eventId);
        });
    }

    @Transactional
    public void addCouponToUserCoupons(User user, CouponResponseDto couponResponseDto) {
        List<CouponEmbeddable> userCoupons = user.getCoupons();

        Long userId = user.getId();
        Long couponId = couponResponseDto.getId();
        if (isUserCouponAllocated(userId, couponId)) {
            throw new CouponDuplicateAllocateException(userId, couponId);
        }
        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    public Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId).orElseThrow(() -> new CouponNotFoundException(couponId));
    }

    public Coupon findCouponByIdWithPessimisticLock(Long couponId) {
        return couponRepository.findByIdWithPessimisticLock(couponId).orElseThrow(() -> new CouponException.CouponNotFoundException(couponId));
    }

    public void removePendingEvent(Long eventId) {
        stringRedisTemplate.opsForSet().remove(RedisCouponConstants.REDIS_PENDING_EVENT_KEY, eventId.toString());
        stringRedisTemplate.opsForHash().delete("PENDING:" + eventId);
    }
}
