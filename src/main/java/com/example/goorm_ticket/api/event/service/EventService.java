package com.example.goorm_ticket.api.event.service;

import com.example.goorm_ticket.domain.event.dto.EventResponseDto;
import com.example.goorm_ticket.domain.event.dto.SeatResponseDto;
import com.example.goorm_ticket.domain.event.entity.Event;
import com.example.goorm_ticket.domain.event.entity.Seat;
import com.example.goorm_ticket.domain.event.repository.EventRepository;
import com.example.goorm_ticket.domain.event.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;


    // 페이징된 이벤트 리스트 조회, DTO 변환
    public Page<EventResponseDto> getAllEvents(int page, int size) {
        Page<Event> eventsPage = eventRepository.findAll(PageRequest.of(page, size));
        List<EventResponseDto> dtoList = eventsPage.stream()
                .map(event -> mapToEventResponseDto(event)
                )
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, eventsPage.getPageable(), eventsPage.getTotalElements());
    }

    public Optional<EventResponseDto> getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .map(this::mapToEventResponseDto);
    }

    @Transactional(readOnly = true)
    public List<SeatResponseDto> getSeatsByEventId(Long eventId) {
        // 해당 이벤트가 존재하는지 확인
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾지 못했습니다. eventId: " + eventId));

        // 해당 이벤트의 모든 좌석 조회
        List<Seat> seats = seatRepository.findByEvent(event);

        // Seat 엔티티를 SeatResponseDto로 변환하여 반환
        return seats.stream()
                .map(seat -> SeatResponseDto.builder()
                        .seatNumber(seat.getSeatNumber())
                        .seatStatus(seat.getSeatStatus())
                        .build())
                .collect(Collectors.toList());
    }

    private EventResponseDto mapToEventResponseDto(Event event) {
        return EventResponseDto.builder()
                .title(event.getTitle())
                .ticketOpenTime(event.getTicketOpenTime())
                .build();
    }

}