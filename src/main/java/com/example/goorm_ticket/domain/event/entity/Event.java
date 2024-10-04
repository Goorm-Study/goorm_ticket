package com.example.goorm_ticket.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String artist;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private int ticketPrice;

    @Column(nullable = false)
    private LocalDateTime ticketOpenTime;

    @Builder(access = AccessLevel.PRIVATE)
    private Event(String artist, String title, String description, String duration, String venue, int ticketPrice, LocalDateTime ticketOpenTime) {
        this.artist = artist;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.venue = venue;
        this.ticketPrice = ticketPrice;
        this.ticketOpenTime = ticketOpenTime;
    }

    public static Event of(String artist, String title, String description, String duration, String venue, int ticketPrice, LocalDateTime ticketOpenTime) {
        return Event.builder()
                .artist(artist)
                .title(title)
                .description(description)
                .duration(duration)
                .venue(venue)
                .ticketPrice(ticketPrice)
                .ticketOpenTime(ticketOpenTime)
                .build();
    }
}
