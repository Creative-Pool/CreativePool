package com.creativepool.controller;

import com.creativepool.constants.ReachOutStatus;
import com.creativepool.entity.ClientReachOut;
import com.creativepool.entity.FreelancerReachOut;
import com.creativepool.entity.UserType;
import com.creativepool.models.*;

import com.creativepool.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/creative-pool/")
@CrossOrigin(origins = "*")
@Slf4j
public class TicketController {

    @Autowired
    private TicketService ticketService;

//    @Autowired
//    CloudStorageService cloudStorageService;

    //done
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
    //client
    @PutMapping("/edit-ticket")
    public ResponseEntity<TicketResponseDTO> editTicket(@ModelAttribute TicketForm ticketForm) throws IOException {
        TicketDTO ticketDTO = ticketForm.getTicketDTO();
        List<MultipartFile> files = ticketForm.getFiles();
        TicketResponseDTO updatedTicket = ticketService.editTicket(ticketDTO, files);
        return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
    }

    //delete
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() throws MalformedURLException {
        List<TicketResponseDTO> tickets = ticketService.getAllTickets();
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }
    //client
    @PostMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponseDTO> assignTicket(@PathVariable UUID ticketId, @RequestParam UUID freelancerId) throws IOException {
        TicketResponseDTO assignedTicket = ticketService.assignTicket(ticketId, freelancerId);
        return ResponseEntity.ok(assignedTicket);
    }
    // done
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    //delete
//    @PostMapping("/{ticketId}/apply")
//    public ResponseEntity<Void> applyForTicket(@PathVariable UUID ticketId, @RequestParam UUID freelancerId, @RequestParam UserType userType) {
//        ticketService.applyForTicket(freelancerId, ticketId,userType);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }
    //done
    @PostMapping("/ticket/search")
    public ResponseEntity<PaginatedResponse<TicketSearchResponse>> searchTickets(@RequestBody TicketSearchRequest ticketSearchRequest) throws IOException {
        log.info("T {}",ticketSearchRequest);
        try {
            PaginatedResponse<TicketSearchResponse> pp = ticketService.searchTickets(ticketSearchRequest);
            log.info("PP {}", pp);
            return new ResponseEntity<>(pp, HttpStatus.OK);
        }
        catch (Exception ex){
            log.error("ex", ex);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }


//    @PostMapping("/freelancer-tickets")
//    public  ResponseEntity<PaginatedResponse<TicketSearchResponse>> getFreelancerTickets(@RequestBody TicketSearchRequest ticketSearchRequest) throws IOException {
//        PaginatedResponse<TicketSearchResponse> pp = ticketService.searchFreelancerTicket(ticketSearchRequest);
//        return new ResponseEntity<>(pp, HttpStatus.OK);
//    }
    //client
    @PostMapping("/ticket/client-reach-out")
    public ResponseEntity<ClientReachOut> clientReachOutToFreelancerForTicket(@RequestBody ClientReachOut clientReachOut) {
        ClientReachOut savedReachOut = ticketService.createClientReachOut(clientReachOut);
        return ResponseEntity.ok(savedReachOut);
    }
    // done
    @GetMapping("/ticket/freelancer-received")
    public ResponseEntity<PaginatedResponse<TicketResponseDTO>> fetchTicketsReceivedByFreelancers(@RequestParam(name = "freelancerId") UUID freelancerId,@RequestParam(name = "page") Integer page,@RequestParam(name = "size") Integer size) throws IOException {
        return new ResponseEntity<>(ticketService.fetchTicketsReceivedByFreelancers(freelancerId,page,size),HttpStatus.OK);
    }
    //client
    @GetMapping("/ticket/freelancers-reached-out")
    public ResponseEntity<PaginatedResponse<Profile> > getFreelancersReachedOutByClient(@RequestParam(name = "ticketId") UUID ticketId,@RequestParam(name = "page") Integer page,@RequestParam(name = "size") Integer size) throws IOException {
        return new ResponseEntity<>(ticketService.getFreelancersReachedOutByClient(ticketId,page,size),HttpStatus.OK);
    }

//    @GetMapping("/ticket/client-reach-out")
//    public ResponseEntity<PaginatedResponse<TicketResponseDTO> > getFreelancersReachedOutByClientForTickets(@RequestParam(name = "freelancerId") UUID freelancerId,@RequestParam(name = "page") Integer page,@RequestParam(name = "size") Integer size) throws IOException {
//        return new ResponseEntity<>(ticketService.getClientReachOut(freelancerId,page,size),HttpStatus.OK);
//    }

    // done
    @PostMapping("/ticket/freelancer-apply")
    public ResponseEntity<FreelancerReachOut> freelancerApplyForTicket(@RequestBody FreelancerReachOut freelancerReachOut) {
        log.info("lili:{}", freelancerReachOut);
        FreelancerReachOut savedReachOut = ticketService.createFreelancerReachOut(freelancerReachOut);
        log.info("pp:{}", savedReachOut);
        return ResponseEntity.ok(savedReachOut);
    }

    //done
    @GetMapping("/ticket/freelancer-applied")
    public ResponseEntity<PaginatedResponse<TicketResponseDTO> > fetchTicketsAppliedByFreelancers(@RequestParam(name = "freelancerId") UUID freelancerId, @RequestParam(name = "reachOutStatus") ReachOutStatus reachOutStatus, @RequestParam(name = "page") Integer page, @RequestParam(name = "size") Integer size) throws IOException {
        log.info("xoo:{},{},{}", freelancerId,page,size);
        PaginatedResponse<TicketResponseDTO> xoxo = ticketService.fetchTicketsAppliedByFreelancers(freelancerId,reachOutStatus,page,size);
        log.info("xoox:{}", xoxo);
        return new ResponseEntity<>(xoxo,HttpStatus.OK);
    }

    //client
    @GetMapping("/ticket/applicants")
    public ResponseEntity<PaginatedResponse<Profile> > getApplicantsForTickets(@RequestParam(name = "ticketId") UUID ticketId,@RequestParam(name = "page") Integer page,@RequestParam(name = "size") Integer size) throws IOException {
        return new ResponseEntity<>(ticketService.getApplicantsForTickets(ticketId,page,size),HttpStatus.OK);
    }

    @PostMapping("/freelancer/reject")
    public ResponseEntity<Void> rejectFreelancerRequest(@RequestParam(name = "ticketId") UUID ticketId,@RequestParam(name = "freelancerId") UUID freelancerId) {
        ticketService.rejectFreelancerRequest(ticketId,freelancerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/client/reject")
    public ResponseEntity<Void> rejectClientRequest(@RequestParam(name = "ticketId") UUID ticketId,@RequestParam(name = "freelancerId") UUID freelancerId) {
        ticketService.rejectClientRequest(ticketId,freelancerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @PutMapping("/ticket/close")
    public ResponseEntity<Void> closeTicket(@RequestParam UUID ticketId) {
        ticketService.markTicketAsClosed(ticketId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/freelancer/backoff")
    public ResponseEntity<Void> backoffFromTicket(@RequestParam UUID ticketId,@RequestParam UserType userType) {
        ticketService.backoffFromTicket(ticketId,userType);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }







//    @GetMapping("/sign-url")
//    public ResponseEntity<String> getSignURL() throws MalformedURLException {
//        String tickets = cloudStorageService.generateSignedUrlForUpload();
//        return new ResponseEntity<>(tickets, HttpStatus.OK);
//    }
}
