package com.creativepool.entity;


import com.creativepool.constants.ReachOutStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity(name = "client_reach_out")
@Data
@NoArgsConstructor
public class ClientReachOut {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "freelancer_id")
    private UUID freelancerId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "reached_out_at")
    private LocalDateTime reachedOutAt = LocalDateTime.now();

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "reach_out_status")
    private ReachOutStatus reachOutStatus;

}
