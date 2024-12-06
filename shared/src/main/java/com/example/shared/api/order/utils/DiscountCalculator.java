package com.example.shared.api.order.utils;

public class DiscountCalculator {
    public static int calculateDiscount(int ticketPrice, double discountRate) {
        return (int) Math.floor(ticketPrice * discountRate / 100);
    }
}