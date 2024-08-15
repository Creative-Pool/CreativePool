package com.creativepool.repository;

import com.creativepool.entity.FreelancerReachOut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FreelancerReachOutRepository extends JpaRepository<FreelancerReachOut, UUID> {


    @Query(value = "select a.firstname,a.lastname,f.freelancer_id from freelancer_reach_out fro join freelancer f on fro.freelancer_id=f.freelancer_id join account a on f.user_id=a.user_id where fro.ticket_id=:ticketId",nativeQuery = true)
    public Page<Object[]> getFreelancersName(UUID ticketId, Pageable pageable);

}
