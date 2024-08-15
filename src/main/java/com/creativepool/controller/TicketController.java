package com.creativepool.controller;

import com.creativepool.entity.ClientReachOut;
import com.creativepool.entity.FreelancerReachOut;
import com.creativepool.entity.UserType;
import com.creativepool.models.*;
import com.creativepool.service.TicketService;
import jdk.jfr.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/creative-pool/")
@CrossOrigin(origins = "*")
@Slf4j
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/create-ticket")
    public ResponseEntity<List<TicketResponseDTO>> createTicket(
            @ModelAttribute TicketForm ticketForm) throws IOException {

        TicketDTO ticketDTO = ticketForm.getTicketDTO();
        List<MultipartFile> files = ticketForm.getFiles();

        log.info("TicketDTO {} files: {}", ticketDTO, files);
        List<TicketResponseDTO> ticketResponseDTOS = new ArrayList<>();
        try {
            TicketResponseDTO createdTicket = ticketService.createTicket(ticketDTO, files);
            ticketResponseDTOS.add(createdTicket);
            return new ResponseEntity<>(ticketResponseDTOS, HttpStatus.CREATED);
        } catch (Exception ex) {
            log.error("Exception", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/edit-ticket")
    public ResponseEntity<TicketResponseDTO> editTicket(@ModelAttribute TicketForm ticketForm) throws IOException {
        TicketDTO ticketDTO = ticketForm.getTicketDTO();
        List<MultipartFile> files = ticketForm.getFiles();
        TicketResponseDTO updatedTicket = ticketService.editTicket(ticketDTO, files);
        return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
    }


    @GetMapping("/tickets")
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() throws MalformedURLException {
        List<TicketResponseDTO> tickets = ticketService.getAllTickets();
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @PostMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponseDTO> assignTicket(@PathVariable UUID ticketId, @RequestParam UUID freelancerId) throws IOException {
        TicketResponseDTO assignedTicket = ticketService.assignTicket(ticketId, freelancerId);
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
        log.info("T {}",ticketSearchRequest);
        try {
            PaginatedResponse<TicketSearchResponse> pp = ticketService.searchUser(ticketSearchRequest);
            log.info("PP {}", pp);
            return new ResponseEntity<>(pp, HttpStatus.OK);
        }
        catch (Exception ex){
            log.error("ex", ex);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }


    @PostMapping("/freelancer-tickets")
    public  ResponseEntity<PaginatedResponse<TicketSearchResponse>> getFreelancerTickets(@RequestBody TicketSearchRequest ticketSearchRequest) throws IOException {
        PaginatedResponse<TicketSearchResponse> pp = ticketService.searchFreelancerTicket(ticketSearchRequest);
        return new ResponseEntity<>(pp, HttpStatus.OK);
    }

    @PostMapping("/ticket/client-reach-out")
    public ResponseEntity<ClientReachOut> createClientReachOut(@RequestBody ClientReachOut clientReachOut) {
        ClientReachOut savedReachOut = ticketService.createClientReachOut(clientReachOut);
        return ResponseEntity.ok(savedReachOut);
    }

    @PostMapping("/ticket/freelancer-reach-out")
    public ResponseEntity<FreelancerReachOut> createFreelancerReachOut(@RequestBody FreelancerReachOut freelancerReachOut) {
        FreelancerReachOut savedReachOut = ticketService.createFreelancerReachOut(freelancerReachOut);
        return ResponseEntity.ok(savedReachOut);
    }

    @GetMapping("/ticket/client-reach-out")
    public ResponseEntity<PaginatedResponse<TicketResponseDTO> > getClientReachOut(@RequestParam(name = "freelancerId") UUID freelancerId,@RequestParam(name = "page") Integer page,@RequestParam(name = "size") Integer size) throws IOException {
    return new ResponseEntity<>(ticketService.getClientReachOut(freelancerId,page,size),HttpStatus.OK);

    }

    @GetMapping("/ticket/freelancer-reach-out")
    public ResponseEntity<PaginatedResponse<Profile> > getFreelancerReachOut(@RequestParam(name = "ticketId") UUID ticketId,@RequestParam(name = "page") Integer page,@RequestParam(name = "size") Integer size) throws IOException {
        return new ResponseEntity<>(ticketService.getFreelancerReachOut(ticketId,page,size),HttpStatus.OK);
    }
}
