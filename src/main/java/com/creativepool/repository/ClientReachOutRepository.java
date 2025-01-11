package com.creativepool.repository;


import com.creativepool.constants.ReachOutStatus;
import com.creativepool.entity.ClientReachOut;
import com.creativepool.entity.FreelancerReachOut;
import com.creativepool.entity.Ticket;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientReachOutRepository extends JpaRepository<ClientReachOut, UUID> {


    @Query(value = "select t.* from client_reach_out cro join ticket t on cro.ticket_id=t.ticket_id where  cro.freelancer_id=:uuid and cro.reach_out_status=:reachOutStatus order by cro.reached_out_at desc",nativeQuery = true)
    public Page<Object[]> getClientReachOutTickets(UUID uuid, ReachOutStatus reachOutStatus, Pageable pageable);


    @Query(value = "select a.firstname,a.lastname,f.freelancer_id from client_reach_out cro join freelancer f on cro.freelancer_id=f.freelancer_id join account a on f.user_id=a.user_id where cro.ticket_id=:ticketId and cro.reach_out_status=:reachOutStatus",nativeQuery = true)
    public Page<Object[]> getFreelancersName(UUID ticketId, Pageable pageable,ReachOutStatus reachOutStatus);

    @Modifying
    @Transactional
    @Query(value = "delete from client_reach_out cro where cro.ticket_id=:ticketId and cro.freelancer_id=:freelancerId",nativeQuery = true)
    public void deleteAppliedTicket(UUID ticketId,UUID freelancerId);

    @Transactional
    public void deleteByTicketId(UUID ticketId);

    @Modifying
    @Transactional
    @Query(value ="update client_reach_out set reach_out_status=:reachOutStatus where freelancer_id=:freelancerId and ticket_id=:ticketId",nativeQuery = true)
    public void updateReachOutStatus(UUID ticketId,UUID freelancerId,Integer reachOutStatus);

    @Transactional
    public Optional<ClientReachOut> findByTicketIdAndFreelancerId(UUID freelancerId, UUID clientId);



}