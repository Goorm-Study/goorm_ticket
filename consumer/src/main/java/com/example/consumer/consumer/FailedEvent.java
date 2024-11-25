package com.example.consumer.consumer;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

@Entity
@RequiredArgsConstructor
public class FailedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String message;

    private LocalDateTime timestamp;

    public FailedEvent(Long userId, String message) {
        this.userId = userId;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
