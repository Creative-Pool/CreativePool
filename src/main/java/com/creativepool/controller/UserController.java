package com.creativepool.controller;


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
    public ResponseEntity<Void> createProfile(@RequestPart("profile") Profile profile, @RequestPart(value = "file", required = false) MultipartFile file) {
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


}
