package com.creativepool.repository;


import com.creativepool.entity.ClientReachOut;
import com.creativepool.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientReachOutRepository extends JpaRepository<ClientReachOut, UUID> {


    @Query(value = "select t.* from client_reach_out cro join ticket t on cro.ticket_id=t.ticket_id where t.ticket_status!=3 order by cro.reached_out_at desc",nativeQuery = true)
    public Page<Object[]> getClientReachOutTickets(UUID uuid, Pageable pageable);

}