package com.creativepool.controller;

import com.creativepool.entity.UserType;
import com.creativepool.models.*;
import com.creativepool.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/creative-pool/")
public class TicketController {


    @Autowired
    private TicketService ticketService;

    @PostMapping("/create-ticket")
    public ResponseEntity<TicketResponseDTO> createTicket( @RequestPart("ticketDTO") TicketDTO ticketDTO,
                                                           @RequestPart(value = "files",required = false) List<MultipartFile> files) throws IOException {
        TicketResponseDTO createdTicket = ticketService.createTicket(ticketDTO,files);
        return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
    }

    @PutMapping("/edit-ticket")
    public ResponseEntity<TicketResponseDTO> editTicket(@RequestPart("ticketDTO") TicketDTO ticketDTO,
                                                        @RequestPart(value = "files",required = false) List<MultipartFile> files) throws IOException {
        TicketResponseDTO updatedTicket = ticketService.editTicket(ticketDTO, files);
        return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
    }


    @GetMapping("/tickets")
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() throws MalformedURLException {
        List<TicketResponseDTO> tickets = ticketService.getAllTickets();
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @PostMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponseDTO> assignTicket(@PathVariable UUID ticketId, @RequestParam UUID userId) throws IOException {
        TicketResponseDTO assignedTicket = ticketService.assignTicket(ticketId, userId);
        return ResponseEntity.ok(assignedTicket);
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{ticketId}/apply")
    public ResponseEntity<Void> applyForTicket(@PathVariable UUID ticketId, @RequestParam UUID freelancerId, @RequestParam UserType userType) {
        ticketService.applyForTicket(freelancerId, ticketId,userType);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/ticket/search")
    public ResponseEntity<PaginatedResponse<TicketSearchResponse>> searchUser(@RequestBody TicketSearchRequest ticketSearchRequest) throws IOException {
        return new ResponseEntity<>(ticketService.searchUser(ticketSearchRequest),HttpStatus.OK);

    }




}
