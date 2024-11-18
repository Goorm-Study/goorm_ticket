package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.aop.annotation.DistributedLock;
import com.example.goorm_ticket.aop.annotation.NamedLock;
import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponRequestDto;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import com.example.goorm_ticket.kafka.CouponKafkaProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CouponKafkaProducer couponKafkaProducer;
    private final ObjectMapper objectMapper;
    private final RedisScript<Long> decrAndSaveMessageScript;

    private static final String REDIS_COUPON_PREFIX = "COUPON:";
    private static final String REDIS_USER_PREFIX = "USER:";
    private static final String REDIS_PENDING_MESSAGE_PREFIX = "PENDING:";

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

    public CouponRequestDto decreaseCouponFromRedis(Long userId, Long couponId, Long quantity) {
        // 발생할 수 있는 예외 1. 쿠폰id에 해당하는 쿠폰이 없음, 2. 쿠폰 수량 부족, 3. 쿠폰 중복 발행 시도
        Long result = stringRedisTemplate.execute(decrAndSaveMessageScript,
                List.of(getRedisCouponKey(couponId), getRedisUserKey(userId), getRedisPendingMessageKey(couponId)),
                couponId.toString(),
                userId.toString());
        return switch (result.intValue()) {
            case -1 -> throw new CouponNotFoundException(couponId);
            case -2 -> throw new CouponQuantityShortageException(0L, 1L);
            case -3 -> throw new CouponDuplicateAllocateException(userId, couponId);
            default -> CouponRequestDto.of(userId, couponId);
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

    // @DistributedLock(key = "'coupon' + #couponId")
    public CouponResponseDto allocateRedisCouponToUser(Long userId, Long couponId) {
        // user 있는지 조회해야 하는데 이걸 어캐하는게 좋을까...레디스에 다 저장하기엔 메모리가 부족하지 않나
        CouponRequestDto couponRequestDto = decreaseCouponFromRedis(userId, couponId, 1L); // 이거 예외 발생했을 때 왜 나머지 코드가 실행되는거지??
        // 메시지를 발행하기 전 서버가 다운되는 경우를 예방하기 위해 수량 감소랑 PENDING 상태로 메시지를 redis에 저장을 원자적으로 처리,
        // 이후 PENDING 상태인 메시지가 남아있으면 메시지 발행을 재시도 하거나 수량을 올려서 의미적 롤백
        if (couponRequestDto == null) {
            return null;
        }
        // TODO: 메시지큐에 쿠폰 발급 이벤트 발행, AOP로 분리하자
        String message = createMessage(couponRequestDto);
        couponKafkaProducer.publishEvent(message);
        return CouponResponseDto.of(couponId);
    }

    private String createMessage(CouponRequestDto couponRequestDto) {
        String message = null;
        try {
            message = objectMapper.writeValueAsString(couponRequestDto);
        } catch (JsonProcessingException e) {
            throw new CouponEventSerializationException();
        }
        return message;
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

    private static String getRedisUserKey(Long userId) {
        return REDIS_USER_PREFIX + userId;
    }

    private static String getRedisPendingMessageKey(Long couponId) {
        return REDIS_PENDING_MESSAGE_PREFIX + couponId;
    }

//    private Coupon cacheCouponToRedis(String key, Long couponId) {
//        Coupon coupon = findCouponById(couponId);
//        stringRedisTemplate.opsForHash().put(key, "couponId", couponId.toString());
//        stringRedisTemplate.opsForHash().put(key, "quantity", coupon.getQuantity().toString());
//        return coupon;
//    }
}
