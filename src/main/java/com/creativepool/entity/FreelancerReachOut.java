package com.creativepool.entity;

import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity(name = "freelancer_reach_out")
@Data
@NoArgsConstructor
public class FreelancerReachOut {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "freelancer_id")
    private UUID freelancerId;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "reached_out_at")
    private LocalDateTime reachedOutAt = LocalDateTime.now();
}