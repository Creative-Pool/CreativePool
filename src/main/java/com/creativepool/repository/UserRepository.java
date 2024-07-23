package com.creativepool.repository;

import com.creativepool.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public UserEntity findByUsername(String username);


   @Query(value = "SELECT * FROM public.search_user_data(:rating,:min_charges,:max_charges, :page, :size)", nativeQuery = true)
   List<Object[]> searchUserData(
            @Param("rating") BigDecimal rating,
            @Param("min_charges") BigDecimal min_charges,
            @Param("max_charges") BigDecimal max_charges,
            @Param("page") int page,
            @Param("size") int size);
}
