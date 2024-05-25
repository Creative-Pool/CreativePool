package com.creativepool.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "FreelancerAreaOfInterest")
public class FreelancerAreaOfInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "FreelancerID")
    private UUID freelancerId;

    @Column(name = "AreaOfInterestID")
    private UUID areaOfInterestId;

}
