package com.creativepool.models;

import com.creativepool.entity.TicketStatus;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class TicketResponseDTO {
    private UUID ticketID;
    private String title;
    private String description;
    private String reporterName;
    private Date createdDate;
    private Double price;
    private Date ticketDeadline;
    private String images;
    private String url;
    private TicketStatus ticketStatus;
    private UUID freelancerId;
    private UUID clientId;
}