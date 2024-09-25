package com.example.goorm_ticket.domain.user.repository;

import com.example.goorm_ticket.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface UserRepository extends JpaRepository<User, Long> {
}
