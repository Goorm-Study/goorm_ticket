package com.example.shared.domain.order.repository;

import com.example.shared.domain.order.entity.Order;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 비관적 락 적용
    Optional<Order> findById(Long id);
}
