package com.creativepool.repository;


import com.creativepool.entity.ClientFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientFeedbackRepository extends JpaRepository<ClientFeedback, UUID> {

}

