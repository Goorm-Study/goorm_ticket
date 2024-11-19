package com.example.goorm_ticket.domain.order.repository;

import com.example.goorm_ticket.domain.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 비관적 락 적용
    Optional<Order> findById(Long id);
}
