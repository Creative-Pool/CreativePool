package com.creativepool.repository;

import com.creativepool.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FcmTokenRepository  extends JpaRepository<FcmToken,UUID> {

    FcmToken findByUserIdAndDeviceId(UUID userId,UUID deviceId);

    @Query(value = "Select fcm_token from fcm_tokens where user_id=:userId",nativeQuery = true)
    List<String> findByUserId(UUID userId);

    @Query(value="Select fcm.fcm_token from fcm_tokens fcm join account ac on ac.user_id=fcm.user_id join client c on c.user_id=c.user_id where c.client_id=:clientId",nativeQuery = true)
    List<String> findClientTokens(UUID clientId);

    @Query(value="Select fcm.fcm_token from fcm_tokens fcm join account ac on ac.user_id=fcm.user_id join freelancer fc on fc.user_id=ac.user_id where fc.freelancer_id=:freelancerId",nativeQuery = true)
    List<String> findFreelancerTokens(UUID freelancerId);

}
