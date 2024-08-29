package com.creativepool.repository;

import com.creativepool.entity.TicketResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface TicketResultRepository  extends JpaRepository<TicketResult, UUID> {

    @Query(value = "SELECT * FROM ticket_result WHERE ticket_id = :ticketId", nativeQuery = true)
    public Page<TicketResult> getTicketResult(UUID ticketId, Pageable pageable);

}
