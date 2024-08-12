package com.creativepool.models;

import com.creativepool.entity.TicketStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketSearchRequest {

    Double rating;

    String complexity;

    String priceRange;

    TicketStatus ticketStatus;

    String dates;

    UUID clientId;

    UUID freelancerId;

    Integer page;

    Integer size;


}
