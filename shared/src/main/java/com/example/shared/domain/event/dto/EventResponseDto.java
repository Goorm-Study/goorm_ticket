package com.example.shared.domain.event.dto;

import com.example.shared.domain.event.entity.Event;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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