package com.creativepool.repository;


import com.creativepool.entity.ClientReachOut;
import com.creativepool.entity.Ticket;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientReachOutRepository extends JpaRepository<ClientReachOut, UUID> {


    @Query(value = "select t.* from client_reach_out cro join ticket t on cro.ticket_id=t.ticket_id where t.ticket_status!=3 and cro.freelancer_id=:uuid order by cro.reached_out_at desc",nativeQuery = true)
    public Page<Object[]> getClientReachOutTickets(UUID uuid, Pageable pageable);


    @Query(value = "select a.firstname,a.lastname,f.freelancer_id from client_reach_out cro join freelancer f on cro.freelancer_id=f.freelancer_id join account a on f.user_id=a.user_id where cro.ticket_id=:ticketId",nativeQuery = true)
    public Page<Object[]> getFreelancersName(UUID ticketId, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "delete from client_reach_out cro where cro.ticket_id=:ticketId and cro.freelancer_id=:freelancerId",nativeQuery = true)
    public void deleteAppliedTicket(UUID ticketId,UUID freelancerId);


}