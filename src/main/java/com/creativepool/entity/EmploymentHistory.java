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

    @Column(name = "Freelancer_ID")
    private UUID freelancerId;

    @Column(name = "PastWork")
    private String pastWork;

    @Column(name = "URLs")
    private String urls;
}
