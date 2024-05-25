package com.creativepool.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Ticket")

public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ticketid", columnDefinition = "uuid", updatable = false)
    private UUID ticketID;

    @Column(name = "Title")
    private String title;

    @Column(name = "Description")
    private String description;

    @Column(name = "reporter_name")
    private String reporterName;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "Price")
    private Double price;

    @Column(name = "ticket_deadline")
    private Date ticketDeadline;

    @Column(name = "Images")
    private String images;

    @Column(name = "URL")
    private String url;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ticket_status")
    private TicketStatus ticketStatus;

    @Column(name = "Freelancer_ID")
    private UUID freelancerId;

    @Column(name = "client_id")
    private UUID clientId;
}
