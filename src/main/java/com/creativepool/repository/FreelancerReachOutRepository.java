package com.creativepool.repository;

import com.creativepool.entity.FreelancerReachOut;
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
public interface FreelancerReachOutRepository extends JpaRepository<FreelancerReachOut, UUID> {


    @Query(value = "select t.* from freelancer_reach_out fro join ticket t on fro.ticket_id=t.ticket_id where  fro.freelancer_id=:uuid and fro.reach_out_status=:reachOutStatus order by fro.reached_out_at desc",nativeQuery = true)
    public Page<Object[]> getTicketsAppliedByFreelancer(UUID uuid, Pageable pageable,Integer reachOutStatus);

    @Query(value = "select a.firstname,a.lastname,f.freelancer_id from freelancer_reach_out fro join freelancer f on fro.freelancer_id=f.freelancer_id join account a on f.user_id=a.user_id  where fro.ticket_id=:ticketId and fro.reach_out_status=0",nativeQuery = true)
    public Page<Object[]> getFreelancersName(UUID ticketId, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "delete from freelancer_reach_out fro where fro.ticket_id=:ticketId and fro.freelancer_id=:freelancerId",nativeQuery = true)
    public void deleteAppliedTicket(UUID ticketId,UUID freelancerId);

    @Transactional
    public void deleteByTicketId(UUID ticketId);

    @Modifying
    @Transactional
    @Query(value ="update freelancer_reach_out set reach_out_status=:reachOutStatus where freelancer_id=:freelancerId and ticket_id=:ticketId",nativeQuery = true)
    public void updateReachOutStatus(UUID ticketId,UUID freelancerId,Integer reachOutStatus);

    @Transactional
    public Optional<FreelancerReachOut> findByTicketIdAndFreelancerId(UUID freelancerId, UUID clientId);



}
