package com.creativepool.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkHistory {
    private String ticketTitle;
    private String ticketComplexity;
    private Double rating;
}
