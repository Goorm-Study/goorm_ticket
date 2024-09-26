package com.example.goorm_ticket.domain.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class EventResponseDto {

    private String title;
    private LocalDateTime ticketOpenTime;

    @Builder
    private EventResponseDto(String title, LocalDateTime ticketOpenTime) {
        this.title = title;
        this.ticketOpenTime = ticketOpenTime;
    }
}