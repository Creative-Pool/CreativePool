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
    @Column(name = "ticket_id", columnDefinition = "uuid", updatable = false)
    private UUID ticketID;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "reporter_name")
    private String reporterName;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "price")
    private Double price;

    @Column(name = "ticket_deadline")
    private Date ticketDeadline;

    @Column(name = "filename")
    private String filename;

    @Column(name = "url")
    private String url;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ticket_status")
    private TicketStatus ticketStatus;

    @Column(name = "freelancer_id")
    private UUID freelancerId;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "complexity")
    private String ticketComplexity;

    @Column(name="budget")
    private Double budget;

    @Column(name="assignee")
    private String assignee;

    @Column(name="meeting_url")
    private String meetingUrl;

}
