package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class RedisCouponServiceTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private RedisCouponService redisCouponService;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @DisplayName("큐에 사용자의 요청이 들어가는지 확인한다.")
    @Test
    void testAddToQueue(){
        // given
        Long userId = 1L;
        Long couponId = 1L;
        User user = User.of("userA", "1111");

        // userRepository에서 user 반환 설정
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 대기열에 값이 존재하지 않는 경우(null)
        when(zSetOperations.rank("wait-queue", userId + ":" + couponId)).thenReturn(null);

        // when
        redisCouponService.addToQueue(userId, couponId);

        // then
        //verify(zSetOperations).add("wait-queue", userId + ":" + couponId, System.currentTimeMillis());
    }



}