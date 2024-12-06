package com.example.shared.api.user.service;

import com.example.shared.domain.order.entity.Order;
import com.example.shared.domain.order.repository.OrderRepository;
import com.example.shared.domain.user.dto.UserInfoResponseDto;
import com.example.shared.domain.user.dto.UserOrderResponseDto;
import com.example.shared.domain.user.dto.UserRegisterRequestDto;
import com.example.shared.domain.user.dto.UserRegisterResponseDto;
import com.example.shared.domain.user.entity.User;
import com.example.shared.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
