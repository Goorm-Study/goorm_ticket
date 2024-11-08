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
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class RedisCouponServiceTest {

    @Autowired
    private UserRepository userRepository;

    @InjectMocks
    private RedisCouponService redisCouponService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @DisplayName("큐에 사용자의 요청이 들어가는지 확인한다.")
    @Test
    void testAddToQueue(){
     //given
//        User userA = User.of("userA", "1111");
//        User userB = User.of("userB", "1111");
//        User userC = User.of("userC", "1111");
//        User userD = User.of("userD", "1111");
//        User userE = User.of("userE", "1111");
//
//        userRepository.save(userA);
//        userRepository.save(userB);
//        userRepository.save(userC);
//        userRepository.save(userD);
//        userRepository.save(userE);

     //when
        redisCouponService.addToQueue(1L, 1L);

     //then
        verify(listOperations).rightPush("coupon-queue", "1:1");
    }



}