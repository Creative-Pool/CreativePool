package com.creativepool.repository;

import com.creativepool.entity.Freelancer;
import com.creativepool.models.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FreelancerRepository extends JpaRepository<Freelancer, UUID> {

    @Query(value = "select a.firstname,a.lastname, f.total_assigned_ticket from FREELANCER f join account a on f.user_id=a.user_id where f.freelancer_id=:freelancerId",nativeQuery = true)
    public List<Object[]> getFreelancerNameAndTotalTicketsAssigned(UUID freelancerId);

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

    @Query(value = "select ac.user_id,ac.firstname,ac.lastname,ac.city,ac.email,ac.date_of_birth,ac.gender,ac.phone,ac.username,ac.filename,ac.user_type,f.rating,f.bio,f.educational_qualification,f.min_charges,f.freelancer_id from account ac left join freelancer f on f.user_id=ac.user_id where ac.phone=:phoneNo and ac.user_Type=:userType",nativeQuery = true)
    public List<Object[]> findFreelancerByPhoneNo(String phoneNo,Integer userType);

    @Query(value = "select t.title,t.complexity,ff.overall_rating from ticket t join freelancer_feedback ff on t.freelancer_id=ff.freelancer_id where t.freelancer_id=:freelancerId and t.ticket_status=:ticketStatus order by t.created_date desc",nativeQuery = true)
    public List<Object[]> getWorkHistory(UUID freelancerId,Integer ticketStatus);

    public Optional<Freelancer> findByUserID(UUID userId);


}
