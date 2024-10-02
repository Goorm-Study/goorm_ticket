package com.example.goorm_ticket.domain.event.repository;

import com.example.goorm_ticket.domain.event.entity.Event;
import com.example.goorm_ticket.domain.event.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEvent(Event event);
}
