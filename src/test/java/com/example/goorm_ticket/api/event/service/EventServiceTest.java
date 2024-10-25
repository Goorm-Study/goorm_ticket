package com.example.goorm_ticket.api.event.service;

import com.example.goorm_ticket.domain.event.dto.EventResponseDto;
import com.example.goorm_ticket.domain.event.dto.SeatResponseDto;
import com.example.goorm_ticket.domain.event.entity.Event;
import com.example.goorm_ticket.domain.event.entity.Seat;
import com.example.goorm_ticket.domain.event.entity.SeatStatus;
import com.example.goorm_ticket.domain.event.repository.EventRepository;
import com.example.goorm_ticket.domain.event.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional  // 테스트가 끝나면 데이터베이스 롤백
public class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    @DisplayName("이벤트 목록을 모두 조회한다")
    void getAllEvents() {
        //given
        Event event1 = createEvent("Test Event1");
        Event event2 = createEvent("Test Event2");

        eventRepository.saveAll(List.of(event1, event2));

        // when
        Page<EventResponseDto> eventResponseDtos = eventService.getAllEvents(0, 10);

        // then
        assertEquals(2, eventResponseDtos.getTotalElements());
        assertEquals("Test Event1", eventResponseDtos.getContent().get(0).getTitle());
        assertEquals("Test Event2", eventResponseDtos.getContent().get(1).getTitle());
    }

    @Test
    @DisplayName("특정 ID로 공연 상세정보를 성공적으로 조회한다.")
    void getEventById_Success() {
        // given
        Event event = createEvent("Test Event");
        eventRepository.save(event);

        // when
        EventResponseDto responseDto = eventService.getEventById(event.getId());

        // then
        assertNotNull(responseDto);
        assertEquals(event.getTitle(), responseDto.getTitle());
        assertEquals(event.getTicketOpenTime(), responseDto.getTicketOpenTime());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 이벤트 조회 시 예외가 발생한다.")
    void getEventById_EventNotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            eventService.getEventById(nonExistentId);
        });

        assertEquals("이벤트를 찾지 못했습니다. eventId: " + nonExistentId, exception.getMessage());
    }

    @Test
    @DisplayName("특정 이벤트의 전체 좌석을 조회한다.")
    void getSeatsByEventId() {
        //given
        Event event = createEvent("Test Event");
        eventRepository.save(event);

        // 테스트용 좌석 저장
        Seat seat1 = Seat.builder()
                .seatNumber(1L)
                .seatSection("A")
                .seatStatus(SeatStatus.AVAILABLE)
                .event(event)
                .build();

        Seat seat2 = Seat.builder()
                .seatNumber(2L)
                .seatSection("A")
                .seatStatus(SeatStatus.RESERVED)
                .event(event)
                .build();
        seatRepository.saveAll(List.of(seat1, seat2));

        // when
        List<SeatResponseDto> seats = eventService.getSeatsByEventId(event.getId());

        // then
        assertNotNull(seats);
        assertEquals(2, seats.size());  // 좌석이 2개인 것을 검증
        assertEquals(1L, seats.get(0).getSeatNumber());  // 첫 번째 좌석 번호가 1인지 검증
        assertEquals(SeatStatus.AVAILABLE, seats.get(0).getSeatStatus());  // 첫 번째 좌석 상태가 AVAILABLE인지 검증
        assertEquals(2L, seats.get(1).getSeatNumber());  // 두 번째 좌석 번호가 2인지 검증
        assertEquals(SeatStatus.RESERVED, seats.get(1).getSeatStatus());  // 두 번째 좌석 상태가 RESERVED인지 검증
    }


    private Event createEvent(String title) {
        return Event.of( "artist", title, "description", "9/28~9/30", "venue", 10000, LocalDateTime.now());
    }

}