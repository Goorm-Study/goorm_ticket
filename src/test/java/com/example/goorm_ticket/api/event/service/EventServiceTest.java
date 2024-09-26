package com.example.goorm_ticket.api.event.service;

import com.example.goorm_ticket.domain.event.dto.EventResponseDto;
import com.example.goorm_ticket.domain.event.entity.Event;
import com.example.goorm_ticket.domain.event.repository.EventRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional  // 테스트가 끝나면 데이터베이스 롤백
public class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

//    @BeforeEach
//    void setUp() {
//
//    }

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

    private Event createEvent(String title) {
        return Event.builder()
                .title(title)
                .artist("artist")
                .description("description")
                .duration("9/28~9/30")
                .venue("venue")
                .ticketPrice(10000)
                .ticketOpenTime(LocalDateTime.now())
                .build();
    }
}