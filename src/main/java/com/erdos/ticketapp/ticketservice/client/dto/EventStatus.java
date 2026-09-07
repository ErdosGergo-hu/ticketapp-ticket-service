package com.erdos.ticketapp.ticketservice.client.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED,
    COMPLETED,

    @JsonEnumDefaultValue
    UNKNOWN
}