package com.example.shared.domain.event.dto;

import com.example.shared.domain.event.entity.SeatStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatResponseDto {
    private Long seatNumber;
    private SeatStatus seatStatus;

    @Builder
    private SeatResponseDto(Long seatNumber, SeatStatus seatStatus) {
        this.seatNumber = seatNumber;
        this.seatStatus = seatStatus;
    }
}