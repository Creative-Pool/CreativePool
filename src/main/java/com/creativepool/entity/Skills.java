package com.creativepool.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Skills")
public class Skills {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "skill_name")
    private String skillName;

    @Column(name = "year_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "freelancer_id")
    private UUID freelancerId;
}
