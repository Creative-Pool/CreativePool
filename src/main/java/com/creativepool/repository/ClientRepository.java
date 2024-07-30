package com.creativepool.repository;

import com.creativepool.entity.Client;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;
@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query("select c.rating  from Client c where c.clientID=:clientId")
    public BigDecimal fetchClientRating(@Param("clientId")UUID clientId);

    @Modifying
    @Transactional
    @Query("update Client c set c.rating=:rating where c.clientID=:clientId")
    public void updateRating(@Param("rating")Double rating,@Param("clientId") UUID clientId);
}
