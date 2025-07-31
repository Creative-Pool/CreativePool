package com.creativepool.repository;

import com.creativepool.entity.AppToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppTokenRepository extends JpaRepository<AppToken, Long> {
    Optional<AppToken> findByProvider(String provider);
}
