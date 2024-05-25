package com.creativepool.repository;

import com.creativepool.entity.Freelancer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface FreelancerRepository extends JpaRepository<Freelancer, UUID> {

    @Query("select f.totalAssignedTickets from FREELANCER f where f.id=:freelancerId")
    public Integer getTotalTicketsAssigned(UUID freelancerId);

    @Modifying
    @Transactional
    @Query("update  FREELANCER f set f.totalAssignedTickets=:totalAssignedTickets where f.id=:freelancerId")
    public int updateTotalTicketsAssigned(int totalAssignedTickets,UUID freelancerId);

}
