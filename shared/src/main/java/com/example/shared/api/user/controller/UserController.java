package com.example.shared.api.user.controller;

import com.example.shared.api.user.service.UserService;
import com.example.shared.domain.user.dto.UserInfoResponseDto;
import com.example.shared.domain.user.dto.UserOrderResponseDto;
import com.example.shared.domain.user.dto.UserRegisterRequestDto;
import com.example.shared.domain.user.dto.UserRegisterResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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



    // 주문 전체 조회
    @GetMapping("/mypage/orders")
    public List<UserOrderResponseDto> getAllOrders(@RequestParam Long user_id) {
        return userService.findAllOrders(user_id);
    }

    // 주문 세부 조회
    @GetMapping("/mypage/orders/{order_id}")
    public UserOrderResponseDto getUserOrder(@PathVariable long order_id) {
        return userService.findOrderById(order_id);
    }
}
