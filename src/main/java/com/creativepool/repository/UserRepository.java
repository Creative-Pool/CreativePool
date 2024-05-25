package com.creativepool.repository;

import com.creativepool.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public UserEntity findByUsername(String username);
}
