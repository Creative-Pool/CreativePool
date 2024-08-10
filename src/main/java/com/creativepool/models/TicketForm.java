package com.creativepool.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
@Data
public class TicketForm {

    private String ticketDTO;
    private List<MultipartFile> files;

    // Getters and Setters

    public TicketDTO getTicketDTO() {
        ObjectMapper ob = new ObjectMapper();
        try {
            return ob.readValue(ticketDTO, TicketDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}