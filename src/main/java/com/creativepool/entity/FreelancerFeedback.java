package com.creativepool.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "freelancer_feedback")
@Data
public class FreelancerFeedback {

    @Id
    @GeneratedValue(generator = "UUID",strategy = GenerationType.AUTO)
    @Column(name = "feedback_id", updatable = false, nullable = false)
    private UUID feedbackId;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "freelancer_id", nullable = false)
    private UUID freelancerId;

    @Column(name = "overall_rating", nullable = false)
    private BigDecimal overallRating;

    @Column(name = "project_outcome")
    private String projectOutcome;

    @Column(name = "creativity_rating", nullable = false)
    private BigDecimal creativityRating;

    @Column(name = "technical_skills_rating", nullable = false)
    private BigDecimal technicalSkillsRating;

    @Column(name = "communication_rating", nullable = false)
    private BigDecimal communicationRating;

    @Column(name = "responsiveness_rating", nullable = false)
    private BigDecimal responsivenessRating;

    @Column(name = "professionalism_rating", nullable = false)
    private BigDecimal professionalismRating;

    @Column(name = "deadline_adherence")
    private Boolean deadlineAdherence;

    @Column(name = "time_management_rating", nullable = false)
    private BigDecimal timeManagementRating;

    @Column(name = "strengths")
    private String strengths;

    @Column(name = "areas_for_improvement")
    private String areasForImprovement;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    // Getters and setters
}

