package com.example.goorm_ticket.domain.event.dto;

import com.example.goorm_ticket.domain.event.entity.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventResponseDto {

    private String title;
    private LocalDateTime ticketOpenTime;

    @Builder
    private EventResponseDto(String title, LocalDateTime ticketOpenTime) {
        this.title = title;
        this.ticketOpenTime = ticketOpenTime;
    }

    public static EventResponseDto mapToEventResponseDto(Event event) {
        return EventResponseDto.builder()
                .title(event.getTitle())
                .ticketOpenTime(event.getTicketOpenTime())
                .build();
    }
}