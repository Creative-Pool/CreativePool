package com.creativepool.repository;

import com.creativepool.entity.TicketResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketResultRepository  extends JpaRepository<TicketResult, UUID> {
}
