package com.creativepool.repository;

import com.creativepool.entity.Client;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query("select c.rating  from Client c where c.clientID=:clientId")
    public BigDecimal fetchClientRating(@Param("clientId")UUID clientId);

    @Modifying
    @Transactional
    @Query("update Client c set c.rating=:rating where c.clientID=:clientId")
    public void updateRating(@Param("rating")Double rating,@Param("clientId") UUID clientId);

    @Query(value = "select ac.user_id,ac.firstname,ac.lastname,ac.city,ac.email,ac.date_of_birth,ac.gender,ac.phone,ac.username,ac.filename,ac.user_type,c.rating,c.client_id from account ac left join client c on c.user_id=ac.user_id where ac.phone=:phoneNo and ac.user_Type=:userType",nativeQuery = true)
    public List<Object[]> findClientByPhoneNo(String phoneNo, Integer userType);

    public Optional<Client> findByUserID(UUID userId);
}
