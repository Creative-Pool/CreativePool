package com.creativepool.repository;

import com.creativepool.entity.Ticket;
import com.creativepool.models.TicketSearchResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {


    @Query(value = "SELECT * FROM public.search_ticket_data(:complexity, :budgetMin, :budgetMax, :ticketStatus, :rating, :startDate, :endDate,:clientId,:page, :size)", nativeQuery = true)
    List<Object[]> searchTickets(
            @Param("complexity") String complexity,
            @Param("budgetMin") BigDecimal budgetMin,
            @Param("budgetMax") BigDecimal budgetMax,
            @Param("ticketStatus") Integer ticketStatus,
            @Param("rating") BigDecimal rating,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("clientId") UUID clientId,
            @Param("page") Integer page,
            @Param("size") Integer size
    );


    @Query(value = "select count(*) from ticket where freelancer_id= :freelancerId",nativeQuery = true)
    public BigInteger fetchTotalTicketsAssignedToFreelancer(UUID freelancerId);

    @Query(value = "select count(*) from ticket where client_id= :clientId",nativeQuery = true)
    public BigInteger fetchTotalTicketsAssignedToClient(UUID clientId);

    @Query(value = "SELECT * FROM public.search_freelancer_ticket_data(:complexity, :budgetMin, :budgetMax, :ticketStatus, :rating, :startDate, :endDate,:freelancerId,:page, :size)", nativeQuery = true)
    List<Object[]> searchFreelancerTickets(
            @Param("complexity") String complexity,
            @Param("budgetMin") BigDecimal budgetMin,
            @Param("budgetMax") BigDecimal budgetMax,
            @Param("ticketStatus") Integer ticketStatus,
            @Param("rating") BigDecimal rating,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("freelancerId") UUID freelancerId,
            @Param("page") Integer page,
            @Param("size") Integer size
    );


}
