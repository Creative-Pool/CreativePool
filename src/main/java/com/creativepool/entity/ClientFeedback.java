package com.creativepool.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;


import java.math.BigDecimal;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "client_feedback")
@Data
public class ClientFeedback {

    @Id
    @GeneratedValue(generator = "UUID",strategy = GenerationType.AUTO)
    @Column(name = "feedback_id", updatable = false, nullable = false)
    private UUID feedbackId;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "overall_rating", nullable = false)
    private BigDecimal overallRating;

    @Column(name = "communication_rating", nullable = false)
    private BigDecimal communicationRating;

    @Column(name = "responsiveness_rating", nullable = false)
    private BigDecimal responsivenessRating;

    @Column(name = "professionalism_rating", nullable = false)
    private BigDecimal professionalismRating;

    @Column(name = "payment_timeliness")
    private Boolean paymentTimeliness;

    @Column(name = "project_scope_rating", nullable = false)
    private BigDecimal projectScopeRating;

    @Column(name = "strengths")
    private String strengths;

    @Column(name = "areas_for_improvement")
    private String areasForImprovement;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    // Getters and setters
}

