package org.example.consumer.repository;

import org.example.consumer.consumer.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedEventRepository extends JpaRepository<FailedEvent, Long> {
}
