package com.example.goorm_ticket.domain.order.repository;

import com.example.goorm_ticket.domain.event.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
