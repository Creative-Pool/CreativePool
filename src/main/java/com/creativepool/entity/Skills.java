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
    @Column(name = "ID")
    private UUID id;

    @Column(name = "SkillName")
    private String skillName;

    @Column(name = "YearsOfExperience")
    private Integer yearsOfExperience;

    @Column(name = "Freelancer_ID")
    private UUID freelancerId;
}
