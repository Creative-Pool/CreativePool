package com.creativepool.controller;


import com.creativepool.entity.UserEntity;
import com.creativepool.entity.UserType;
import com.creativepool.models.PaginatedResponse;
import com.creativepool.models.Profile;
import com.creativepool.models.User;
import com.creativepool.models.UserSearchRequest;
import com.creativepool.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/creative-pool/")
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/create-user")
    public ResponseEntity<List<UserEntity>> createUser(@RequestBody User user) {
        log.info("user:{}",user);
        try {
            List<UserEntity> users = userService.createUser(user);
            return new ResponseEntity<>(users, HttpStatus.CREATED);
        }
        catch (Exception ex){
            log.error("user:{}",ex);
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/create-profile")
    public ResponseEntity<Void> createProfile(@RequestPart("profile") Profile profile, @RequestPart(value = "file", required = false) MultipartFile file) {
        userService.createProfile(profile, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    public ResponseEntity<List<Profile>> getProfile(@RequestParam(name = "phoneNo") String phoneNo, @RequestParam(name = "userType") UserType userType) {
        List<Profile> profiles = userService.getProfile(phoneNo, userType);
        return new ResponseEntity<>(profiles, HttpStatus.OK);
    }

    @PostMapping("/user/search")
    public ResponseEntity<PaginatedResponse<Profile>> searchUser(@RequestBody UserSearchRequest userSearchRequest) {
        return new ResponseEntity<>(userService.searchUser(userSearchRequest), HttpStatus.OK);

    }


}
