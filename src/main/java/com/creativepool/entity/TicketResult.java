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
@Entity(name = "ticket_result")
public class TicketResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private UUID resultId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "filename")
    private String filename;

    @Column(name = "last_upload")
    private Date lastUpload;
}
