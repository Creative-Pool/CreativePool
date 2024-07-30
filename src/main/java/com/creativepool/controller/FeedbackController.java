package com.creativepool.controller;

import com.creativepool.models.ClientFeedbackRequest;
import com.creativepool.models.FreelancerFeedbackRequest;
import com.creativepool.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {


    @Autowired
    private FeedbackService service;

    @PostMapping("/submitFreelancerFeedback")
    public ResponseEntity<String> submitFreelancerFeedback(@RequestBody FreelancerFeedbackRequest request) {
        service.submitFreelancerFeedback(request);
        return ResponseEntity.ok( "Response submitted successfully");
    }

    @PostMapping("/submitClientFeedback")
    public ResponseEntity<String> submitClientFeedback(@RequestBody ClientFeedbackRequest request) {
        service.submitClientFeedback(request);
        return ResponseEntity.ok( "Response submitted successfully");
    }
}
