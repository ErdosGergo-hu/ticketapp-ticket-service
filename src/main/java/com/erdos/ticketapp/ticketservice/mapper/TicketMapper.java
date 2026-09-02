package com.erdos.ticketapp.ticketservice.mapper;

import com.erdos.ticketapp.ticketservice.dto.response.TicketResponse;
import com.erdos.ticketapp.ticketservice.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TicketMapper {

    TicketResponse toResponse(Ticket ticket);
}
