package com.creativepool.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "freelancerticketapplicants")
public class FreelancerTicketApplicants {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "freelancer_id")
    private UUID freelancerID;

    @Column(name = "ticket_id")
    private UUID  ticketID;

    @Column(name="user_type")
    @Enumerated(EnumType.ORDINAL)
    private UserType userType;

}
