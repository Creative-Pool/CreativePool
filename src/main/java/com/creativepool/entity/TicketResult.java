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
    @Column(name = "result_id")
    private Integer resultId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "image_url")
    private String imageURL;

    @Column(name = "video_url")
    private String videoURL;
}
