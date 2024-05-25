package com.creativepool.repository;

import com.creativepool.entity.FreelancerTicketApplicants;
import com.creativepool.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FreelancerTicketApplicantsRepository extends JpaRepository<FreelancerTicketApplicants, UUID> {
    void deleteByTicketID(UUID ticketId);

    @Query("select fta from freelancer_ticketapplicants fta where fta.ticketID=: ticketId and fta.userType=: userType")
    Optional<FreelancerTicketApplicants>  findTicketApplicants(UUID ticketId, UserType userType);
}
