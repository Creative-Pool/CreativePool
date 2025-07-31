package com.creativepool.service;

import com.creativepool.constants.CreativepoolConstants;
import com.creativepool.constants.Errors;
import com.creativepool.entity.AppToken;
import com.creativepool.exception.CreativePoolException;
import com.creativepool.repository.AppTokenRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class GoogleMeetService {

    Logger logger = LoggerFactory.getLogger(GoogleMeetService.class);

    private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    @Value("${google.meet.credentials}")
    private String googleMeetCredentialsPath;

    @Value("${token.directory.path}")
    private String getTokensDirectoryPath;

    @Autowired
    private AppTokenRepository appTokenRepository;

    
    public Credential getCredentials() throws IOException, GeneralSecurityException {
        // Load client secrets from JSON
        InputStream clientSecretStream =
                getClass().getClassLoader().getResourceAsStream(googleMeetCredentialsPath);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                jsonFactory,
                new InputStreamReader(clientSecretStream)
        );

        List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/calendar");

        // Create the authorization flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                clientSecrets,
                SCOPES
        )
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        // ✅ Check if token already exists in DB
        Optional<AppToken> tokenOpt = appTokenRepository.findByProvider("google_calendar");

        if (tokenOpt.isPresent()) {
            AppToken token = tokenOpt.get();

            // ✅ Manually build Credential with token server URL
            Credential credential = new Credential.Builder(flow.getMethod())
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(jsonFactory)
                    .setClientAuthentication(flow.getClientAuthentication())
                    .setTokenServerEncodedUrl("https://oauth2.googleapis.com/token") // ✅ Required
                    .build();

            credential.setRefreshToken(token.getRefreshToken());

            // ✅ Generate a new access token
            credential.refreshToken();

            // ✅ Save new access token and expiry
            token.setAccessToken(credential.getAccessToken());
            token.setExpiresAt(Instant.now().plusSeconds(3500)); // ~1 hour validity
            appTokenRepository.save(token);

            return credential;
        } else {
            // ✅ First-time OAuth browser flow
            Credential credential = new AuthorizationCodeInstalledApp(
                    flow,
                    new LocalServerReceiver.Builder().setPort(8090).build()
            ).authorize("user");

            // ✅ Save refresh token for future use
            AppToken token = AppToken.builder()
                    .provider("google_calendar")
                    .refreshToken(credential.getRefreshToken())
                    .accessToken(credential.getAccessToken())
                    .expiresAt(Instant.now().plusSeconds(3500))
                    .build();

            appTokenRepository.save(token);

            return credential;
        }
    }






    public Calendar getCalendarService() throws GeneralSecurityException, IOException {
        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, getCredentials())
                .setApplicationName(CreativepoolConstants.APPLICATION_NAME)
                .build();
    }

    public String createInstantMeeting(List<String> attendeeEmails) throws GeneralSecurityException, IOException {
        try {
            logger.info("Action to create instant meeting started");
            Calendar service = getCalendarService();
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            Event event = new Event()
                    .setSummary(CreativepoolConstants.MEETING_SUMMARY)
                    .setDescription(CreativepoolConstants.MEETING_DESCRIPTION)
                    .setVisibility("public")
                    .setGuestsCanModify(true)
                    .setGuestsCanSeeOtherGuests(true)
                    .setStart(new EventDateTime().setDateTime(new DateTime(now.toInstant().toString())).setTimeZone("Asia/Kolkata"))
                    .setEnd(new EventDateTime().setDateTime(new DateTime(now.plusHours(1).toInstant().toString())).setTimeZone("Asia/Kolkata"))
                    .setConferenceData(new ConferenceData()
                            .setCreateRequest(new CreateConferenceRequest()
                                    .setRequestId("meet" + System.currentTimeMillis())
                                    .setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"))))
                    .setOrganizer(new Event.Organizer().setDisplayName(CreativepoolConstants.CREATIVE_POOL));


            List<EventAttendee> attendees = new ArrayList<>();
            for (String email : attendeeEmails) {
                attendees.add(new EventAttendee().setEmail(email).setResponseStatus("accepted"));
            }
            event.setAttendees(attendees);

            event = service.events().insert("primary", event).setConferenceDataVersion(1).setSendUpdates("none")
                    .execute();

            return event.getHangoutLink();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new CreativePoolException(Errors.E00026.getMessage());
        }
    }
}
