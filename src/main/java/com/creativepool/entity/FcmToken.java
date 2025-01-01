package com.creativepool.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;


@Entity(name = "fcm_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;  // Auto-generated primary key

    @Column(name = "user_id", nullable = false)
    private UUID userId;  // User ID associated with the device

    @Column(name = "device_id", nullable = false, unique = true)
    private UUID deviceId;  // Unique identifier for the device

    @Column(name = "fcm_token", nullable = false)
    private String fcmToken;  // FCM token for the device

    // Optional: You can add additional methods or annotations if needed
}

