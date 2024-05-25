package com.creativepool.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "FREELANCER")
public class Freelancer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "USER_ID")
    private UUID userID;

    @Column(name = "Rating")
    private Double rating;

    @Column(name = "BIO")
    private String bio;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "educationalqualification")
    private EducationalQualificationType educationalQualification;

    @Column(name = "TotalAssignedTickets")
    private Integer totalAssignedTickets;
}
