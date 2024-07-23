package com.creativepool.models;

import com.creativepool.entity.TicketStatus;
import lombok.Data;

@Data
public class TicketSearchRequest {

    Double rating;

    String complexity;

    String priceRange;

    TicketStatus ticketStatus;

    String dates;

    Integer page;

    Integer size;


}
