package com.example.goorm_ticket.domain.event.repository;

import com.example.goorm_ticket.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}