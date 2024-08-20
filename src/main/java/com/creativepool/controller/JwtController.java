package com.creativepool.controller;

import com.creativepool.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwtController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/generate-token")
    public ResponseEntity<String> generateToken(@RequestParam("userId") String userId) {
        String token = jwtUtil.generateToken(userId);
        return ResponseEntity.ok(token);
    }
}
