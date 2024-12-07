package com.creativepool.repository;

import com.creativepool.constants.Status;
import com.creativepool.entity.TicketResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TicketResultRepository  extends JpaRepository<TicketResult, UUID> {

    @Query(value = "SELECT * FROM ticket_result WHERE ticket_id = :ticketId", nativeQuery = true)
    public Page<TicketResult> getTicketResult(UUID ticketId, Pageable pageable);

    @Modifying
    @Query("delete from ticket_result tr where tr.ticketId=:ticketId and tr.status=:status and tr.filename=:filename")
    public void deleteTicketResult(UUID ticketId, Status status,String filename);

    @Query("Select tr from ticket_result tr where tr.filename=:filename")
    public TicketResult findByFileName(String filename);

}
