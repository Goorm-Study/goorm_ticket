package com.example.goorm_ticket.domain.event.repository;

import com.example.goorm_ticket.domain.event.entity.Event;
import com.example.goorm_ticket.domain.event.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEvent(Event event);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s JOIN FETCH s.event WHERE s.id = :seatId")
    Optional<Seat> findByIdWithEventWithPessimisticLock(@Param("seatId") Long seatId);
}
