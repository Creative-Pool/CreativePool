package com.creativepool.models;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class FreelancerFeedbackRequest {
    private UUID ticketId;
    private UUID freelancerId;
    private BigDecimal overallRating;
    private String projectOutcome;
    private BigDecimal creativityRating;
    private BigDecimal technicalSkillsRating;
    private BigDecimal communicationRating;
    private BigDecimal responsivenessRating;
    private BigDecimal professionalismRating;
    private Boolean deadlineAdherence;
    private BigDecimal timeManagementRating;
    private String strengths;
    private String areasForImprovement;

    // Getters and setters
}
