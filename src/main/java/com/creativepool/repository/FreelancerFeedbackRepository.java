package com.creativepool.repository;


import com.creativepool.entity.FreelancerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FreelancerFeedbackRepository extends JpaRepository<FreelancerFeedback, UUID> {

}

