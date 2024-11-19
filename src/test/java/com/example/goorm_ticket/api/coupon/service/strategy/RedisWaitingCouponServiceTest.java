package com.example.goorm_ticket.api.coupon.service.strategy;

import com.example.goorm_ticket.api.coupon.service.RedisWaitingCouponService;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RedisWaitingCouponServiceTest {

    @Autowired
    private RedisWaitingCouponService redisWaitingCouponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        // 테스트용 쿠폰과 사용자 생성
//        coupon = createCoupon(10L); // 총 10개의 쿠폰 설정
//        redisTemplate.opsForValue().set("coupon_count", 10L);
//        for (long i = 1; i <= 100; i++) {
//            userRepository.save(User.builder().username("User" + i).password("1234").build());
//        }

        // 테스트 전에 Redis에서 모든 데이터를 삭제하여 이전 데이터를 지움
        redisTemplate.opsForZSet().removeRange("coupon_queue", 0, -1); // 대기열 초기화
        redisTemplate.opsForValue().set("coupon_count", 30L); // 쿠폰 수량 초기화

    }

    @Test
    public void testAllocateCoupon_AddUserToQueueAndProcessQueue() {
        // 사용자 요청을 대기열에 추가하고 쿠폰 발급 진행
        CouponResponseDto response = redisWaitingCouponService.allocateCoupon(1L, coupon.getId());

        // 응답 결과 확인
        assertNotNull(response);
        assertEquals("쿠폰 요청이 처리되었습니다.", response.getMessage());

        // 쿠폰 수량 감소 확인
        Long remainingCoupons = Long.valueOf(redisTemplate.opsForValue().get("coupon_count"));
        assertEquals(9, remainingCoupons); // 초기값 10에서 1 감소
    }

    @Test
    public void testProcessCouponQueue_ExhaustsCoupons() {
        // 10명의 사용자 추가
        for (int i = 1; i <= 10; i++) {
            redisWaitingCouponService.allocateCoupon((long) i, 10L);
        }

        // 쿠폰 수량이 0인지 확인
        Long remainingCoupons = Long.valueOf(redisTemplate.opsForValue().get("coupon_count"));
        assertEquals(0, remainingCoupons);

        // 추가 요청 처리 - 이미 소진된 상황에서 테스트
        CouponResponseDto response = redisWaitingCouponService.allocateCoupon(11L, 100L);
        remainingCoupons = Long.valueOf(redisTemplate.opsForValue().get("coupon_count"));
        assertEquals(0, remainingCoupons); // 여전히 0

        // 결과 확인
        assertNotNull(response);
        assertEquals("쿠폰 요청이 처리되었습니다.", response.getMessage()); // 처리 메시지는 동일하게 반환
    }

    @Test
    void 선착순_쿠폰_30명에게_10개_제공() throws InterruptedException {
        final int people = 30;
        final int limitCount = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(people);

        // 쿠폰 수량 설정
        redisTemplate.opsForValue().set("coupon_count", (long) limitCount);

        // 사용자들을 대기열에 추가하는 작업
        for (int i = 1; i <= people; i++) {
            final long userId = i;
            new Thread(() -> {
                redisWaitingCouponService.allocateCoupon(userId, 1L);
                countDownLatch.countDown();
            }).start();
        }

        // 모든 쓰레드가 종료될 때까지 기다림
        countDownLatch.await();

        // 쿠폰 발급 후 남은 쿠폰 수 확인
        Long remainingCoupons = redisTemplate.opsForValue().get("coupon_count");
        assertEquals(0, remainingCoupons); // 30개의 쿠폰이 발급되어야 하므로 남은 쿠폰 수는 0이어야 한다.

        // 추가 요청 확인 (쿠폰이 소진된 후에 요청한 사용자들이 대기열에서 발급되지 않음)
        Set<Long> remainingUsers = redisTemplate.opsForZSet().range("coupon_queue", 0, -1);
        assertEquals(20, remainingUsers.size()); // 100명 중 30명이 쿠폰을 받았고, 나머지 70명은 대기열에 있어야 한다.
    }

    @Test
    void 선착순쿠폰_100명에게_30개_제공() throws InterruptedException {
        String COUPON_COUNT_KEY = "coupon_count";
        String COUPON_QUEUE_KEY = "coupon_queue";

        final int people = 100; // 총 사람 수
        final int limitCount = 30; // 제공할 쿠폰 수
        final CountDownLatch countDownLatch = new CountDownLatch(people); // 모든 스레드가 완료될 때까지 기다리기 위한 CountDownLatch

        // 쿠폰 수량 설정
        redisTemplate.opsForValue().set(COUPON_COUNT_KEY, (long) limitCount);

        // 사용자들을 대기열에 추가하는 작업
        List<Thread> workers = Stream
                .generate(() -> new Thread(new AddQueueWorker(countDownLatch)))
                .limit(people)
                .collect(Collectors.toList());

        // 모든 스레드 실행
        workers.forEach(Thread::start);
        countDownLatch.await();
        Thread.sleep(5000); // 기프티콘 발급 스케줄러 작업 시간

        // 쿠폰 발급 후 남은 쿠폰 수 확인
        Long remainingCoupons = redisTemplate.opsForValue().get(COUPON_COUNT_KEY);
        assertEquals(0, remainingCoupons); // 쿠폰이 모두 발급되어야 하므로 남은 쿠폰 수는 0이어야 한다.

        // 대기열에 남은 사용자 수 확인
        Set<Long> remainingUsers = redisTemplate.opsForZSet().range(COUPON_QUEUE_KEY, 0, -1);
        assertEquals(people - limitCount, remainingUsers.size()); // 쿠폰을 받지 못한 사용자 수는 70이어야 한다.
    }

    private class AddQueueWorker implements Runnable {
        private CountDownLatch countDownLatch;

        public AddQueueWorker(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            // 대기열에 사용자 추가 및 쿠폰 발급 프로세스 처리
            redisWaitingCouponService.allocateCoupon(Thread.currentThread().getId(), 1L);
            countDownLatch.countDown();
        }
    }


//    @Test
//    @DisplayName("100개의 스레드에서 동시에 쿠폰(100개)을 요청한다 - Redis 대기열")
//    void allocateCouponTo100ThreadWithRedisQueue() throws InterruptedException {
//        int threadCount = 100;  // 100명의 사용자가 쿠폰 요청
//        CountDownLatch latch = new CountDownLatch(threadCount);
//        ExecutorService executorService = Executors.newFixedThreadPool(32);
//
//        List<CouponResponseDto> results = new ArrayList<>();
//
//        // 초기 쿠폰 수량을 Redis에 설정
//        redisTemplate.opsForValue().set("coupon_count", 100L);
//
//        // 각 사용자 스레드가 동시에 쿠폰 요청
//        for (long i = 1; i <= threadCount; i++) {
//            long userId = i;
//            executorService.submit(() -> {
//                try {
//                    CouponResponseDto response = redisCouponAllocationStrategy.allocateCoupon(userId, coupon.getId());
//                    results.add(response);
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(); // 모든 스레드가 작업을 완료할 때까지 대기
//
//        // 발급 성공한 쿠폰 개수 확인 (최대 100개)
//        long successfulAllocations = results.stream()
//                .filter(response -> response != null && "쿠폰 요청이 처리되었습니다.".equals(response.getMessage()))
//                .count();
//
//        assertThat(successfulAllocations).isEqualTo(100); // 쿠폰 수량만큼만 발급 성공
//
//        // 남은 쿠폰 수량 확인 (쿠폰이 다 발급되어야 하므로 0이어야 함)
//        Long remainingCoupons = redisTemplate.opsForValue().get("coupon_count");
//        assertThat(remainingCoupons).isEqualTo(0L);
//
//        executorService.shutdown();
//    }

    private Coupon createCoupon(Long quantity) {
        Coupon coupon = Coupon.of(quantity, "coupon1", 0.15, LocalDateTime.of(2024, 12, 30, 0, 0));
        return couponRepository.save(coupon);
    }
}