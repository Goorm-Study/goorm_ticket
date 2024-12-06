package com.example.shared.domain.event.repository;

import com.example.shared.domain.event.entity.Event;
import com.example.shared.domain.event.entity.Seat;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEvent(Event event);

    @Lock(LockModeType.PESSIMISTIC_WRITE)  // 비관적 락 적용
    @Query("SELECT s FROM Seat s JOIN FETCH s.event WHERE s.id = :seatId")
    Optional<Seat> findByIdWithEvent(@Param("seatId") Long seatId);
}
