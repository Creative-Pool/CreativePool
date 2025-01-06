package com.creativepool.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FcmTokenRequest {

    private UUID userId;  // User ID associated with the device
    private String deviceId;  // Unique identifier for the device
    private String fcmToken;  // FCM token for the device

}
