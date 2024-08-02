package com.creativepool.controller;


import com.creativepool.entity.UserType;
import com.creativepool.models.PaginatedResponse;
import com.creativepool.models.Profile;
import com.creativepool.models.User;
import com.creativepool.models.UserSearchRequest;
import com.creativepool.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/creative-pool/")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/create-user")
    public ResponseEntity<Void> createUser(@RequestBody User user) {
        userService.createUser(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/create-profile")
    public ResponseEntity<Void> createProfile(@RequestPart("profile") Profile profile, @RequestPart(value = "file", required = false) MultipartFile file) {
        userService.createProfile(profile,file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    public ResponseEntity<Profile> getProfile(@RequestParam(name = "phoneNo") String phoneNo,@RequestParam(name = "userType") UserType userType) {
        Profile profile=userService.getProfile(phoneNo,userType);
        return new ResponseEntity<>(profile,HttpStatus.OK);
    }

    @PostMapping("/user/search")
    public ResponseEntity<PaginatedResponse<Profile>> searchUser(@RequestBody UserSearchRequest userSearchRequest) {
        return new ResponseEntity<>(userService.searchUser(userSearchRequest),HttpStatus.OK);

    }



}
