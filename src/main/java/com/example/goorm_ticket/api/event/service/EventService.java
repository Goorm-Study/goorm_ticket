package com.example.goorm_ticket.api.event.service;

import com.example.goorm_ticket.domain.event.dto.EventResponseDto;
import com.example.goorm_ticket.domain.event.entity.Event;
import com.example.goorm_ticket.domain.event.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // 페이징된 이벤트 리스트 조회, DTO 변환
    public Page<EventResponseDto> getAllEvents(int page, int size) {
        Page<Event> eventsPage = eventRepository.findAll(PageRequest.of(page, size));
        List<EventResponseDto> dtoList = eventsPage.stream()
                .map(event -> EventResponseDto.builder()
                        .title(event.getTitle())
                        .ticketOpenTime(event.getTicketOpenTime())
                        .build()
                )
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, eventsPage.getPageable(), eventsPage.getTotalElements());
    }

    public EventResponseDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with id " + eventId));

        return EventResponseDto.builder()
                .title(event.getTitle())
                .ticketOpenTime(event.getTicketOpenTime())
                .build();
    }

}