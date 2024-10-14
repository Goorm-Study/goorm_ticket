package com.example.goorm_ticket.api.user.service;

import com.example.goorm_ticket.domain.order.entity.Order;
import com.example.goorm_ticket.domain.order.repository.OrderRepository;
import com.example.goorm_ticket.domain.user.dto.*;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
     private final OrderRepository orderRepository;

    public UserRegisterResponseDto registerUser(UserRegisterRequestDto request) {
        User entity = User.of(request.username(), request.password());
        userRepository.save(entity);
        return UserRegisterResponseDto.of(entity);
    }

    public UserInfoResponseDto getUserInfo(Long userId) {
        User entity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserInfoResponseDto.of(entity);
    }

    public List<UserOrderResponseDto> findAllOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        List<UserOrderResponseDto> userOrderResponses = new ArrayList<>();
        for (Order order : orders) {
            userOrderResponses.add(UserOrderResponseDto.of(order));
        }
        return userOrderResponses;
    }


    public UserOrderResponseDto findOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        UserOrderResponseDto userOrderResponse = UserOrderResponseDto.of(order);
        return userOrderResponse;
    }

}
