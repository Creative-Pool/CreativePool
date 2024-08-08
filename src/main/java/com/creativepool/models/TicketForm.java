package com.creativepool.models;

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
        }
        catch (Exception ex){
        }
        return null;
    }

//    public void setTicketDTO(TicketDTO ticketDTO) {
//        this.ticketDTO = ticketDTO;
//    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }
}