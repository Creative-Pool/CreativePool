package com.creativepool.repository;

import com.creativepool.entity.Freelancer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@Repository
public interface FreelancerRepository extends JpaRepository<Freelancer, UUID> {

    @Query("select f.totalAssignedTickets from FREELANCER f where f.id=:freelancerId")
    public Integer getTotalTicketsAssigned(UUID freelancerId);

    @Modifying
    @Transactional
    @Query("update  FREELANCER f set f.totalAssignedTickets=:totalAssignedTickets where f.id=:freelancerId")
    public int updateTotalTicketsAssigned(int totalAssignedTickets,UUID freelancerId);



    @Query("select f.rating  from FREELANCER f where f.id= :freelancerId")
    public BigDecimal fetchFreelancerRating(UUID freelancerId);

    @Modifying
    @Transactional
    @Query("update FREELANCER f set f.rating=:rating where f.id=:freelancerId")
    public void updateRating(Double rating,UUID freelancerId);

    @Query(value = "select f.user_id,f.rating,f.bio,f.educational_qualification,f.min_charges,t.title,t.complexity,ff.overall_rating from freelancer f join ticket t on f.freelancer_id=t.freelancer_id join freelancer_feedback ff \n" +
            "on t.ticket_id=ff.ticket_id where f.freelancer_id='37ea4aa3-261c-4dcc-bcc8-649e94ecffb9' order by t.created_date desc ",nativeQuery = true)
    public List<Object[]> findFreelancerById(UUID freelancerId);

}
