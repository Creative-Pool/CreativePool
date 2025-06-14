package com.creativepool.controller;


import com.creativepool.entity.FcmToken;
import com.creativepool.entity.UserEntity;
import com.creativepool.entity.UserType;
import com.creativepool.exception.CreativePoolException;
import com.creativepool.models.*;
import com.creativepool.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/creative-pool/")
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/create-user")
    public ResponseEntity<List<Profile>> createUser(@RequestBody User user) {
        List<Profile> users = userService.createUser(user);
        return new ResponseEntity<>(users, HttpStatus.CREATED);
    }

    @PostMapping("/create-profile")
    public ResponseEntity<Void> createProfile(@RequestPart(value= "profile", required = false) Profile profile, @RequestPart(value = "file", required = false) MultipartFile file) {
        userService.createProfile(profile, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    public ResponseEntity<List<Profile>> getProfile(@RequestParam(name = "phoneNo") String phoneNo, @RequestParam(name = "userType") UserType userType) throws IOException {
        List<Profile> profiles = userService.getProfile(phoneNo, userType);
        return new ResponseEntity<>(profiles, HttpStatus.OK);
    }

    @PostMapping("/freelancer-search")
    public ResponseEntity<PaginatedResponse<Profile>> searchFreelancer(@RequestBody UserSearchRequest userSearchRequest) throws IOException {
        return new ResponseEntity<>(userService.searchFreelancer(userSearchRequest), HttpStatus.OK);

    }

    @PutMapping ("/edit-profile")
    public ResponseEntity<Void> editProfile(@ModelAttribute ProfileForm profileForm) throws IOException {
        try {
            log.info("Action to edit profile started :{}", profileForm.getProfile());
            Profile profile = profileForm.getProfile();
            MultipartFile file = profileForm.getFile();
            userService.editProfile(profile, file);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            throw new CreativePoolException(ex.getMessage());
        }
    }

    @PostMapping("/fcm-token/store")
    public ResponseEntity<String> upsertFcmToken(@RequestBody FcmTokenRequest fcmTokenRequest) {
        // Call the upsert method from the service to insert or update the token
        String responseMessage = userService.upsertFcmToken(fcmTokenRequest);
        return ResponseEntity.ok(responseMessage);  // Success message
    }

    @GetMapping("/fcm-token")
    public ResponseEntity<List<String>> getFcmTokensByUserId(@RequestParam UUID id,@RequestParam UserType userType) {
        List<String> tokens = userService.getFcmTokensByUserId(id,userType);

        if (tokens.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content if no tokens are found
        }
        return ResponseEntity.ok(tokens); // 200 OK with the list of tokens
    }


}
