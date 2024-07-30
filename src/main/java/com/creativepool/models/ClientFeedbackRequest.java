package com.creativepool.models;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ClientFeedbackRequest {
    private UUID ticketId;
    private UUID clientId;
    private BigDecimal overallRating;
    private BigDecimal communicationRating;
    private BigDecimal responsivenessRating;
    private BigDecimal professionalismRating;
    private Boolean paymentTimeliness;
    private BigDecimal projectScopeRating;
    private String strengths;
    private String areasForImprovement;

    // Getters and setters
}
