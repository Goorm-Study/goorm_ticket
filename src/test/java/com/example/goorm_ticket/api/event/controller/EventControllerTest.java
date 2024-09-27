package com.example.goorm_ticket.api.event.controller;

import com.example.goorm_ticket.api.event.service.EventService;
import com.example.goorm_ticket.domain.event.dto.EventResponseDto;
import com.example.goorm_ticket.domain.event.dto.SeatResponseDto;
import com.example.goorm_ticket.domain.event.entity.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.mockito.Mockito;

import org.springframework.test.web.servlet.ResultActions;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;

@WebMvcTest(EventController.class)
//컨트롤러 통합테스트
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Test
    @DisplayName("전체 이벤트 조회 API 테스트")
    void getAllEvents() throws Exception {
        // given
        EventResponseDto eventResponseDto = EventResponseDto.builder()
                .title("Test Event")
                .ticketOpenTime(LocalDateTime.now())
                .build();

        // when
        when(eventService.getAllEvents(0, 10))
                .thenReturn(new PageImpl<>(Collections.singletonList(eventResponseDto)));

        // then
        mockMvc.perform(get("/events")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Event"));
    }

    @Test
    @DisplayName("특정 이벤트 조회 API 테스트")
    void getEventById() throws Exception {
        // given
        EventResponseDto eventResponseDto = EventResponseDto.builder()
                .title("Test Event")
                .ticketOpenTime(LocalDateTime.now())
                .build();

        // when
        when(eventService.getEventById(1L)).thenReturn(eventResponseDto);

        // then
        mockMvc.perform(get("/events/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Event"));
    }

    @Test
    @DisplayName("특정 이벤트의 좌석 조회 API 테스트")
    void getSeatsByEventId() throws Exception {
        // given
        SeatResponseDto seat1 = SeatResponseDto.builder().seatNumber(1L).seatStatus(SeatStatus.AVAILABLE).build();
        SeatResponseDto seat2 = SeatResponseDto.builder().seatNumber(2L).seatStatus(SeatStatus.RESERVED).build();
        List<SeatResponseDto> seatList = Arrays.asList(seat1, seat2);

        // when
        Mockito.when(eventService.getSeatsByEventId(anyLong())).thenReturn(seatList);

        // then
        mockMvc.perform(get("/events/1/seats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].seatNumber", is(1)))
                .andExpect(jsonPath("$[0].seatStatus", is(SeatStatus.AVAILABLE.toString())))
                .andExpect(jsonPath("$[1].seatNumber", is(2)))
                .andExpect(jsonPath("$[1].seatStatus", is(SeatStatus.RESERVED.toString())))
                .andDo(print());
    }
}
