package com.example.goorm_ticket.api.user.service;

import com.example.goorm_ticket.domain.order.entity.Order;
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
    // private final OrderRepository orderRepository;

    public UserRegisterResponse registerUser(UserRegisterRequest request) {
        User entity = User.builder()
                .username(request.username())
                .password(request.password())
                .build();

        User savedUser = userRepository.save(entity);
        return UserRegisterResponse.of(savedUser);
    }

    public UserInfoResponse getUserInfo(Long userId) {
        User entity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserInfoResponse.of(entity);
    }
//
//    public List<UserOrderResponse> findAllOrders(Long userId) {
//        List<Order> orders = orderRepository.findByUserId(userId);
//        List<UserOrderResponse> userOrderResponses = new ArrayList<>();
//        for (Order order : orders) {
//            userOrderResponses.add(UserOrderResponse.of(order));
//        }
//        return userOrderResponses;
//    }
//
//
//    public UserOrderResponse findOrderById(Long orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
//        UserOrderResponse userOrderResponse = UserOrderResponse.of(order);
//        return userOrderResponse;
//    }

}
