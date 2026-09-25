package com.erdos.ticketapp.ticketservice.enums;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    DEAD
}