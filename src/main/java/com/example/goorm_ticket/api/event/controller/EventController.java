package com.example.goorm_ticket.api.event.controller;

import com.example.goorm_ticket.api.event.service.EventService;
import com.example.goorm_ticket.domain.event.dto.EventResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // 전체 페이지 이벤트 조회 API
    @GetMapping("/events")
    public ResponseEntity<Page<EventResponseDto>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EventResponseDto> events = eventService.getAllEvents(page, size);
        return ResponseEntity.ok(events);
    }

}
