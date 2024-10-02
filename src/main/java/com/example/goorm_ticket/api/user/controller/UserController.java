package com.example.goorm_ticket.api.user.controller;

import com.example.goorm_ticket.api.user.service.UserService;
import com.example.goorm_ticket.domain.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public UserRegisterResponseDto registerUser(@RequestBody UserRegisterRequestDto request) {
        return userService.registerUser(request);
    }

    // 사용자 정보 조회
    @GetMapping("/{user_id}")
    public UserInfoResponseDto getUserInfo(@PathVariable Long user_id) {
        return userService.getUserInfo(user_id);
    }

// 아래 기능은 OrderRepository가 있어야 작동 확인 가능

//
//    // 주문 전체 조회
//    @GetMapping("/mypage/orders")
//    public List<UserOrderResponse> getAllOrders(@RequestParam Long user_id) {
//        return userService.findAllOrders(user_id);
//    }
//
//    // 주문 세부 조회
//    @GetMapping("/mypage/orders/{order_id}")
//    public UserOrderResponse getUserOrder(@PathVariable long order_id) {
//        return userService.findOrderById(order_id);
//    }
}
