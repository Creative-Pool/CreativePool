package com.creativepool.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "freelancer_ticketapplicants")
public class FreelancerTicketApplicants {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "Freelancer_ID")
    private UUID freelancerID;

    @Column(name = "Ticket_ID")
    private UUID  ticketID;

    @Column(name="User_Type")
    @Enumerated(EnumType.ORDINAL)
    private UserType userType;

}
