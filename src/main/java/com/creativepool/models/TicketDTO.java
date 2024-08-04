package com.creativepool.models;

import com.creativepool.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {
    private UUID ticketId;
    private String title;
    private String description;
    private String reporterName;
    private Double price;
    private Date ticketDeadline;
    private String images;
    private String url;
    private UUID clientId;
    private String ticketComplexity;
    private List<String> deleteUrls;
    private TicketStatus ticketStatus;

}
