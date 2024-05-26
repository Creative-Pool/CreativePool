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
    @Column(name = "freelancer_id", columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userID;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "bio")
    private String bio;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "educational_qualification")
    private EducationalQualificationType educationalQualification;

    @Column(name = "total_assigned_ticket")
    private Integer totalAssignedTickets;
}
