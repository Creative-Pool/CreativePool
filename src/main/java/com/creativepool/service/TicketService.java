package com.creativepool.service;


import com.creativepool.constants.Errors;
import com.creativepool.entity.FreelancerTicketApplicants;
import com.creativepool.entity.Ticket;
import com.creativepool.entity.TicketStatus;
import com.creativepool.entity.UserType;
import com.creativepool.exception.BadRequestException;
import com.creativepool.models.TicketDTO;
import com.creativepool.models.TicketResponseDTO;
import com.creativepool.repository.FreelancerRepository;
import com.creativepool.repository.FreelancerTicketApplicantsRepository;
import com.creativepool.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;


import java.util.Arrays;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    FreelancerRepository freelancerRepository;

    @Autowired
    FreelancerTicketApplicantsRepository freelancerTicketApplicantsRepository;

    public TicketResponseDTO createTicket(TicketDTO ticketDTO) {
        Ticket ticket = new Ticket();
        // Map DTO fields to entity
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setReporterName(ticketDTO.getReporterName());
        ticket.setPrice(ticketDTO.getPrice());
        ticket.setTicketDeadline(ticketDTO.getTicketDeadline());
        ticket.setImages(String.join(",", ticketDTO.getImages())); // Convert List to comma-separated String
        ticket.setUrl(ticketDTO.getUrl());
        ticket.setCreatedDate(new Date());
        ticket.setClientId(ticketDTO.getClientId());
        ticket.setTicketStatus(TicketStatus.OPEN); // or any default status

        Ticket savedTicket = ticketRepository.save(ticket);

        return mapToResponseDTO(savedTicket);
    }

    public List<TicketResponseDTO> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream().map(this::mapToResponseDTO).collect(Collectors.toList());

    }

    private TicketResponseDTO mapToResponseDTO(Ticket ticket) {
        TicketResponseDTO responseDTO = new TicketResponseDTO();
        // Map entity fields to DTO
        responseDTO.setTicketID(ticket.getTicketID());
        responseDTO.setTitle(ticket.getTitle());
        responseDTO.setDescription(ticket.getDescription());
        responseDTO.setReporterName(ticket.getReporterName());
        responseDTO.setPrice(ticket.getPrice());
        responseDTO.setTicketDeadline(ticket.getTicketDeadline());
        responseDTO.setUrl(ticket.getUrl());
        responseDTO.setTicketStatus(ticket.getTicketStatus());
        responseDTO.setFreelancerId(ticket.getFreelancerId());
        responseDTO.setClientId(ticket.getClientId());
        return responseDTO;
    }

    public TicketResponseDTO assignTicket(UUID ticketId, UUID freelancerId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new BadRequestException(String.format(Errors.E00004.getMessage(),ticketId)));

        Integer totalTicketsAssigned=freelancerRepository.getTotalTicketsAssigned(freelancerId);

        if(totalTicketsAssigned>5){
            throw new IllegalStateException(Errors.E00008.getMessage());
        }
        freelancerRepository.updateTotalTicketsAssigned(totalTicketsAssigned++,freelancerId);
        ticket.setFreelancerId(freelancerId);
        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToResponseDTO(savedTicket);
    }


    public void applyForTicket(UUID freelancerId, UUID ticketId, UserType userType) {
        FreelancerTicketApplicants application = new FreelancerTicketApplicants();
        application.setFreelancerID(freelancerId);
        application.setTicketID(ticketId);
        application.setUserType(userType);
        freelancerTicketApplicantsRepository.save(application);
    }

    public void getTicketApplicants(UUID ticketId,UserType userType){




    }


    public void deleteTicket(UUID ticketId) {
        // Check if the ticket exists
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new BadRequestException(String.format(Errors.E00004.getMessage(), ticketId)));
        // Delete all applications related to the ticket
        freelancerTicketApplicantsRepository.deleteByTicketID(ticketId);
        // Delete the ticket
        ticketRepository.delete(ticket);
    }
}
