package com.example.goorm_ticket.domain.order.repository;

import com.example.goorm_ticket.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
