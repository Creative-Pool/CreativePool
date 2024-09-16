package com.creativepool.controller;

import com.creativepool.constants.Errors;
import com.creativepool.exception.CreativePoolException;
import com.creativepool.service.GoogleMeetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/creative-pool/")
public class MeetingController {

    @Autowired
    GoogleMeetService googleMeetService;

    @PostMapping("/create-meeting")
    public ResponseEntity<String> createGoogleMeeting(@RequestBody List<String> attendeeEmails) throws GeneralSecurityException, IOException {
        return new ResponseEntity<>(googleMeetService.createInstantMeeting(attendeeEmails), HttpStatus.OK);
    }
}
