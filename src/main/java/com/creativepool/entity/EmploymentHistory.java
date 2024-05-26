package com.creativepool.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "EmploymentHistory")
public class EmploymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "freelancer_id")
    private UUID freelancerId;

    @Column(name = "past_work")
    private String pastWork;

    @Column(name = "urls")
    private String urls;
}
