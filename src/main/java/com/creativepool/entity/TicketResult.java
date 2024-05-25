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
@Entity(name = "TicketResult")
public class TicketResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ResultId")
    private Integer resultId;

    @Column(name = "TICKET_ID")
    private UUID ticketId;

    @Column(name = "ImageURL")
    private String imageURL;

    @Column(name = "VideoURL")
    private String videoURL;
}
