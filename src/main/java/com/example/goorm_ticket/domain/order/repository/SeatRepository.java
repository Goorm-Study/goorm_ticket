package com.example.goorm_ticket.domain.order.repository;

import com.example.goorm_ticket.domain.event.entity.Seat;
import com.example.goorm_ticket.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s JOIN FETCH s.event WHERE s.id = :seatId")
    Optional<Seat> findByIdWithEvent(@Param("seatId") Long seatId);
}
