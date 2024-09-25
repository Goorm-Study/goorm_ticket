insert into users(password, username)
values ('afasd', '이민정'),
       ('123r3', '김민정'),
       ('dlajfdi', '최민정');

INSERT INTO COUPON(discount_rate, expiration_date, quantity, name)
VALUES (5, '2024-09-30', 5000, '9월 특별 할인'),
       (10, '2024-10-30', 1000, '10월 특별 할인');

insert into event(ticket_price, ticket_open_time, artist, description, duration, title, venue)
values (250000, '2024-09-24 12:00:00', '콜드플레이', 'LIVE NATION PRESENTS COLDPLAY : MUSIC OF THE SPHERES DELIVERED BY DHL', '2025.04.16 ~2025.04.22', '콜드플레이 내한공연', '고양종합운동장 주경기장');

insert into seat(event_id, order_id, seat_number, seat_section, seat_status)
values (1, null, 12, 1, 'AVAILABLE'),
       (1, null, 20, 1, 'AVAILABLE'),
       (1, null, 3, 2, 'AVAILABLE');
