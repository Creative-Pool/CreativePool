package com.creativepool.models;

import com.creativepool.entity.TicketStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketSearchRequest {

    Double rating;

    String searchType;

    String complexity;

    String priceRange;

    TicketStatus ticketStatus;

    String dates;

    UUID clientOrFreelancerId;

    Integer page;

    Integer size;


}
