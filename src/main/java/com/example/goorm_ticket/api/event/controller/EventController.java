package com.example.goorm_ticket.api.event.controller;

import com.example.goorm_ticket.api.event.service.EventService;
import com.example.goorm_ticket.domain.event.dto.EventResponseDto;
import com.example.goorm_ticket.domain.event.dto.SeatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // 전체 페이지 이벤트 조회 API
    @GetMapping("/events")
    public ResponseEntity<Page<EventResponseDto>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EventResponseDto> events = eventService.getAllEvents(page, size);
        return ResponseEntity.ok(events);
    }

    // 이벤트ID로 상세 정보 조회
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long eventId) {
        EventResponseDto event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    // 특정 이벤트의 전체 좌석을 조회하는 API
    @GetMapping("/events/{eventId}/seats")
    public ResponseEntity<List<SeatResponseDto>> getSeatsByEventId(@PathVariable("eventId") Long eventId) {
        List<SeatResponseDto> seats = eventService.getSeatsByEventId(eventId);
        return ResponseEntity.ok(seats);
    }

}
