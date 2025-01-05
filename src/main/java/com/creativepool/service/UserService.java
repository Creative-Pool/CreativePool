package com.creativepool.service;

import com.creativepool.constants.Errors;
import com.creativepool.entity.*;
import com.creativepool.exception.BadRequestException;
import com.creativepool.exception.CreativePoolException;
import com.creativepool.exception.ResourceNotFoundException;
import com.creativepool.models.*;
import com.creativepool.repository.ClientRepository;
import com.creativepool.repository.FcmTokenRepository;
import com.creativepool.repository.FreelancerRepository;
import com.creativepool.repository.UserRepository;
import com.creativepool.utils.Utils;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.creativepool.utils.Utils.getOrDefault;
@Slf4j
@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FreelancerRepository freelancerRepository;

    @Autowired
    ClientRepository clientRepository;


    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Value("${credential.file}")
    private String credentials;

    @Value("${project.id}")
    private String projectId;

    @Autowired
    EntityManager entityManager;

    @Autowired
    CloudStorageService cloudStorageService;

    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    Logger logger = LoggerFactory.getLogger(UserService.class);

    public List<Profile> createUser(User user) {
        logger.info("Action to create user started {}", user);
        List<Profile> profiles = new ArrayList<>();
        try {
            if (ObjectUtils.isEmpty(user))
                throw new BadRequestException(Errors.E00001.getMessage());
            UserEntity userEntity = userRepository.findByPhone(user.getPhone());
            if (!ObjectUtils.isEmpty(userEntity))
                throw new BadRequestException(Errors.E00003.getMessage());

            UUID clientId = null;
            UUID freelancerId = null;
            userEntity = new UserEntity();
            userEntity.setUserID(UUID.randomUUID());
            userEntity.setUsername(user.getUsername());
            userEntity.setUserType(user.getUserType());
            userEntity.setCreatedDate(new Date());
            userEntity.setIsActive(true);
            userEntity.setIsDeleted(false);
            userEntity.setEmail(user.getEmail());
            userEntity.setFirstName(user.getFirstName());
            userEntity.setLastName(user.getLastName());
            userEntity.setGender(Gender.MALE);
            userEntity.setPhone(user.getPhone());
            UserEntity userEntity1 = userRepository.save(userEntity);

            if (user.getUserType().equals(UserType.CLIENT)) {
                Client client = new Client();
                client.setClientID(UUID.randomUUID());
                client.setUserID(userEntity.getUserID());
                clientRepository.save(client);
                clientId = client.getClientID();
            } else {
                Freelancer freelancer = new Freelancer();
                freelancer.setId(UUID.randomUUID());
                freelancer.setUserID(userEntity.getUserID());
                freelancerRepository.save(freelancer);
                freelancerId = freelancer.getId();
            }
            Profile profile = toProfile(userEntity, clientId, freelancerId);
            profiles.add(profile);
        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMessage(), e);
            if (e.getMessage().contains("account_email_key"))
                throw new DataIntegrityViolationException(Errors.E00006.getMessage());
            if (e.getMessage().contains("account_phone_key"))
                throw new DataIntegrityViolationException(Errors.E00007.getMessage());
        } catch (BadRequestException ex) {
            logger.error(ex.getMessage(), ex);
            throw new BadRequestException(ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new CreativePoolException(Errors.E00013.getMessage());
        }
        return profiles;
    }

    private Profile toProfile(UserEntity userEntity, UUID clientId, UUID freelancerId) {
        if (userEntity == null) {
            return null;
        }

        Profile profile = new Profile();
        profile.setUsername(userEntity.getUsername());
        profile.setFirstName(userEntity.getFirstName());
        profile.setLastName(userEntity.getLastName());
        profile.setPhone(userEntity.getPhone());
        profile.setEmail(userEntity.getEmail());
        profile.setDateOfBirth(userEntity.getDateOfBirth());
        profile.setGender(userEntity.getGender());
        profile.setCity(userEntity.getCity());
        profile.setUserID(userEntity.getUserID());
        profile.setUserType(userEntity.getUserType());
        profile.setClientId(clientId);
        profile.setFreelancerId(freelancerId);

        return profile;
    }

    public void createProfile(Profile profile, MultipartFile file) {
        try {
            logger.info("Action to create profile started {}", profile);
            switch (profile.getUserType().toString()) {
                case "FREELANCER":
                    createFreelancerProfile(profile, file);
                    break;
                case "CLIENT":
                    createClientProfile(profile, file);
                    break;
            }
            logger.info("Action to create profile completed");
        } catch (BadRequestException ex) {
            logger.error(ex.getMessage(), ex);
            throw new BadRequestException(ex.getMessage());
        } catch (CreativePoolException ex) {
            logger.error(ex.getMessage(), ex);
            throw new CreativePoolException(ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new CreativePoolException(Errors.E00013.getMessage());
        }
    }

    private void createFreelancerProfile(Profile profile, MultipartFile file) throws IOException {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(profile.getUserID());
        UserEntity userEntity;
        if (optionalUserEntity.isPresent()) {
            List<String> filenames = new ArrayList<>();
            if (!file.isEmpty())
                cloudStorageService.uploadFile(file, filenames);

            userEntity = optionalUserEntity.get();
            userEntity.setCity(profile.getCity());
            userEntity.setGender(profile.getGender());
            userEntity.setDateOfBirth(profile.getDateOfBirth());
            userEntity.setFilename(String.join(",", filenames));
            Freelancer freelancer = new Freelancer();
            freelancer.setId(UUID.randomUUID());
            freelancer.setBio(profile.getBio());
            freelancer.setRating(profile.getRating());
            freelancer.setEducationalQualification(profile.getEducationalQualification());
            freelancer.setUserID(profile.getUserID());
            freelancer.setTotalAssignedTickets(0);

            userRepository.save(userEntity);
            freelancerRepository.save(freelancer);
        } else {
            throw new BadRequestException(Errors.E00014.getMessage());
        }
    }

    private void createClientProfile(Profile profile, MultipartFile file) throws IOException {

        Optional<UserEntity> optionalUserEntity = userRepository.findById(profile.getUserID());
        UserEntity userEntity;
        if (optionalUserEntity.isPresent()) {
            userEntity = optionalUserEntity.get();
            List<String> filenames = new ArrayList<>();
            if (!file.isEmpty()) cloudStorageService.uploadFile(file, filenames);

            userEntity.setCity(profile.getCity());
            userEntity.setGender(profile.getGender());
            userEntity.setDateOfBirth(profile.getDateOfBirth());
            userEntity.setFilename(String.join(",", filenames));


            Client client = new Client();
            client.setClientID(UUID.randomUUID());
            client.setRating(profile.getRating());
            client.setUserID(profile.getUserID());

            userRepository.save(userEntity);
            clientRepository.save(client);
        } else {
            throw new BadRequestException(Errors.E00014.getMessage());
        }
    }

    public List<Profile> getProfile(String phoneNo, UserType userType) throws IOException {
        try {
            return switch (userType.toString()) {
                case "FREELANCER" -> getFreelancerProfile(phoneNo, userType);
                case "CLIENT" -> getClientProfile(phoneNo, userType);
                default -> new ArrayList<>();
            };
        } catch (Exception e) {
            logger.error("Error fetching profile for phone number: {} and user type: {}", phoneNo, userType, e);
            throw new CreativePoolException(Errors.E00015.getMessage());
        }
    }


    private List<Profile> getFreelancerProfile(String phoneNo, UserType userType) throws IOException {
        logger.info("Fetching freelancer profile for phone number: {}", phoneNo);

        List<Profile> profiles = new ArrayList<>();
        List<WorkHistory> workHistoryList = new ArrayList<>();
        List<Object[]> freelancerObjectArray = freelancerRepository.findFreelancerByPhoneNo(phoneNo, userType.ordinal());
        if (freelancerObjectArray != null && !freelancerObjectArray.isEmpty()) {
            Profile profile = new Profile();
            List<String> profileImagesUrl = new ArrayList<>();
            Object[] row = freelancerObjectArray.get(0);
            profile.setUserID(row[0] != null ? (UUID) row[0] : null);
            profile.setFirstName(row[1] != null ? (String) row[1] : null);
            profile.setLastName(row[2] != null ? (String) row[2] : null);
            profile.setCity(row[3] != null ? (String) row[3] : null);
            profile.setEmail(row[4] != null ? (String) row[4] : null);
            profile.setDateOfBirth(row[5] != null ? (Date) row[5] : null);

            if (row[6] != null) {
                profile.setGender(Gender.values()[(Integer) row[6]]);
            } else {
                profile.setGender(null); // or set to a default value if needed
            }

            profile.setPhone(row[7] != null ? (String) row[7] : null);
            profile.setUsername(row[8] != null ? (String) row[8] : null);
            if (row[9] != null) {

                String[] files = ((String) row[9]).split(",");
                for (String file : files) {
                    profileImagesUrl.add(cloudStorageService.generateSignedUrl(file));
                }
                profile.setProfileImage(String.join(",", profileImagesUrl));


            }

            if (row[10] != null) {
                profile.setUserType(UserType.values()[(Integer) row[10]]);
            } else {
                profile.setUserType(null); // or set to a default value if needed
            }

            profile.setRating(row[11] != null ? ((BigDecimal) row[11]).doubleValue() : null);
            profile.setBio(row[12] != null ? (String) row[12] : null);

            if (row[13] != null) {
                profile.setEducationalQualification(EducationalQualificationType.values()[(Integer) row[13]]);
            } else {
                profile.setEducationalQualification(null); // or set to a default value if needed
            }

            profile.setMinCharges(row[14] != null ? (BigDecimal) row[14] : null);

            if (row[15] != null) {
                List<Object[]> workHistoryObjectArray = freelancerRepository.getWorkHistory((UUID) row[15], TicketStatus.CLOSED.ordinal());

                for (Object[] object : workHistoryObjectArray) {
                    WorkHistory workHistory = new WorkHistory(object[0] != null ? (String) object[0] : null, object[1] != null ? (String) object[1] : null, object[2] != null ? ((BigDecimal) object[2]).doubleValue() : null);
                    workHistoryList.add(workHistory);
                }

                profile.setWorkHistory(workHistoryList);
                profile.setFreelancerId(row[15] != null ? (UUID) row[15] : null);
            }

            profiles.add(profile);
        }
        return profiles;
    }

    private List<Profile> getClientProfile(String phoneNo, UserType userType) throws IOException {
        logger.info("Fetching client profile for phone number: {}", phoneNo);
        List<Profile> profiles = new ArrayList<>();
        List<Object[]> clientObjectArray = clientRepository.findClientByPhoneNo(phoneNo, userType.ordinal());
        if (clientObjectArray != null && !clientObjectArray.isEmpty()) {
            Profile profile = new Profile();
            List<String> profileImagesUrl = new ArrayList<>();
            Object[] row = clientObjectArray.get(0);
            profile.setUserID(row[0] != null ? (UUID) row[0] : null);
            profile.setFirstName(row[1] != null ? (String) row[1] : null);
            profile.setLastName(row[2] != null ? (String) row[2] : null);
            profile.setCity(row[3] != null ? (String) row[3] : null);
            profile.setEmail(row[4] != null ? (String) row[4] : null);
            profile.setDateOfBirth(row[5] != null ? (Date) row[5] : null);

            if (row[6] != null) {
                profile.setGender(Gender.values()[(Integer) row[6]]);
            } else {
                profile.setGender(null); // or set to a default value if needed
            }

            profile.setPhone(row[7] != null ? (String) row[7] : null);
            profile.setUsername(row[8] != null ? (String) row[8] : null);

            if (row[9] != null) {

                String[] files = ((String) row[9]).split(",");
                for (String file : files) {
                    profileImagesUrl.add(cloudStorageService.generateSignedUrl(file));
                }
                profile.setProfileImage(String.join(",", profileImagesUrl));


            }


            if (row[10] != null) {
                profile.setUserType(UserType.values()[(Integer) row[10]]);
            } else {
                profile.setUserType(null); // or set to a default value if needed
            }

            profile.setRating(row[11] != null ? ((BigDecimal) row[11]).doubleValue() : null);
            profile.setClientId(row[12] != null ? (UUID) row[12] : null);
           
            profiles.add(profile);
        }
        return profiles;
    }


    public PaginatedResponse<Profile> searchFreelancer(UserSearchRequest userSearchRequest) throws IOException {
        try {
            logger.info("Action to search freelancer started {}", userSearchRequest);
            // Validate pagination parameters
            Integer page = userSearchRequest.getPage();
            Integer size = userSearchRequest.getSize();
            if (page == null || size == null) {
                throw new BadRequestException(Errors.E00001.getMessage());
            }

            // Prepare search parameters
            BigDecimal rating = userSearchRequest.getRating() != null ? BigDecimal.valueOf(userSearchRequest.getRating()) : null;
            BigDecimal[] priceRange = parsePriceRange(userSearchRequest.getPriceRange());
            BigDecimal minPrice = priceRange[0];
            BigDecimal maxPrice = priceRange[1];

            String firstname = userSearchRequest.getFirstname() != null ? userSearchRequest.getFirstname() : null;
            String lastname = userSearchRequest.getLastname() != null ? userSearchRequest.getLastname() : null;
            String username = userSearchRequest.getUsername() != null ? userSearchRequest.getUsername() : null;

            // Perform search with pagination
            List<Object[]> result = userRepository.searchFreelancerUserData(rating, minPrice, maxPrice, username, firstname, lastname, page, size);

            // Process search results
            List<Profile> profiles = new ArrayList<>();
            long totalRowCount = 0;

            for (Object[] row : result) {
                Profile profile = mapRowToProfile(row);
                profiles.add(profile);
                totalRowCount = (long) row[15]; // Assuming row count is the last column
            }

            // Calculate pagination details
            boolean isLastPage = ((page + 1) * size >= totalRowCount);
            Integer totalPages = (int) Math.ceil((double) totalRowCount / size);
            logger.info("Action to search freelancer completed");
            return new PaginatedResponse<>(totalRowCount, profiles, isLastPage, page + 1, totalPages);
        } catch (BadRequestException ex) {
            logger.error(ex.getMessage(), ex);
            throw new BadRequestException(ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new CreativePoolException(Errors.E00013.getMessage());
        }

    }

    private BigDecimal[] parsePriceRange(String priceRange) {
        if (priceRange == null) {
            return new BigDecimal[]{null, null};
        }

        String[] parts = priceRange.split("-");
        BigDecimal minPrice = parts.length > 0 ? new BigDecimal(parts[0]) : null;
        BigDecimal maxPrice = parts.length > 1 ? new BigDecimal(parts[1]) : null;

        return new BigDecimal[]{minPrice, maxPrice};
    }

    private Profile mapRowToProfile(Object[] row) throws IOException {
        Profile profile = new Profile();
        profile.setUsername(Utils.getOrDefault((String) row[0], profile.getUsername()));
        profile.setFirstName(Utils.getOrDefault((String) row[1], profile.getFirstName()));
        profile.setLastName(Utils.getOrDefault((String) row[2], profile.getLastName()));
        profile.setPhone(Utils.getOrDefault((String) row[3], profile.getPhone()));
        profile.setEmail(Utils.getOrDefault((String) row[4], profile.getEmail()));


        profile.setProfileImage(Utils.getOrDefault(row[5] != null ? cloudStorageService.generateSignedUrl((String) row[5]) : null, profile.getProfileImage()));
        profile.setDateOfBirth(Utils.getOrDefault((Date) row[6], profile.getDateOfBirth()));
        profile.setGender(Utils.getOrDefault(row[7] != null ? Gender.values()[(Integer) row[7]] : null, profile.getGender()));
        profile.setCity(Utils.getOrDefault((String) row[8], profile.getCity()));
        profile.setUserType(Utils.getOrDefault(row[9] != null ? UserType.values()[(Integer) row[9]] : null, profile.getUserType()));
        profile.setUserID(Utils.getOrDefault((UUID) row[10], profile.getUserID()));
        profile.setBio(Utils.getOrDefault((String) row[11], profile.getBio()));
        profile.setRating(Utils.getOrDefault(row[12] != null ? ((BigDecimal) row[12]).doubleValue() : null, profile.getRating()));
        profile.setMinCharges(Utils.getOrDefault((BigDecimal) row[13], profile.getMinCharges()));
        profile.setFreelancerId(Utils.getOrDefault((UUID) row[10], profile.getFreelancerId()));
        return profile;
    }

    public void editProfile(Profile profile, MultipartFile file) {
        try {
            switch (profile.getUserType().toString()) {
                case "FREELANCER":
                    editFreelancerProfile(profile, file);
                    break;
                case "CLIENT":
                    editClientProfile(profile, file);
                    break;
                default:
                    throw new ResourceNotFoundException("User type not found");
            }
        } catch (ResourceNotFoundException | IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResourceNotFoundException(e.getMessage()); // Assume E00006 is an edit-specific error message
        }
    }

    private void editFreelancerProfile(Profile profile, MultipartFile file) throws IOException {

        Optional<UserEntity> optionalUserEntity = userRepository.findById(profile.getUserID());
        if (optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();
            List<String> filenames = new ArrayList<>();
            if (file != null && !file.isEmpty()) {
                String oldProfilePicture = userEntity.getFilename();
                cloudStorageService.uploadFile(file, filenames);
                userEntity.setFilename(String.join(",", filenames));

                if (!StringUtils.isEmpty(oldProfilePicture))
                    cloudStorageService.deleteFile(bucketName, oldProfilePicture);
            }


            userEntity.setCity(getOrDefault(profile.getCity(), userEntity.getCity()));
            userEntity.setGender(getOrDefault(profile.getGender(), userEntity.getGender()));
            userEntity.setDateOfBirth(getOrDefault(profile.getDateOfBirth(), userEntity.getDateOfBirth()));

            Optional<Freelancer> optionalFreelancer = freelancerRepository.findByUserID(profile.getUserID());
            if (optionalFreelancer.isPresent()) {
                Freelancer freelancer = optionalFreelancer.get();
                freelancer.setBio(getOrDefault(profile.getBio(), freelancer.getBio()));
                freelancer.setRating(getOrDefault(profile.getRating(), freelancer.getRating()));
                freelancer.setEducationalQualification(getOrDefault(profile.getEducationalQualification(), freelancer.getEducationalQualification()));

                userRepository.save(userEntity);
                freelancerRepository.save(freelancer);
            } else {
                throw new ResourceNotFoundException("Freelancer profile not found");
            }
        } else {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private void editClientProfile(Profile profile, MultipartFile file) throws IOException {
        log.info("Action to edit the client started {}", profile);
        Optional<UserEntity> optionalUserEntity = userRepository.findById(profile.getUserID());
        if (optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();
            List<String> filenames = new ArrayList<>();
            if (file != null && !file.isEmpty()) {
                String oldProfilePicture = userEntity.getFilename();
                cloudStorageService.uploadFile(file, filenames);
                userEntity.setFilename(String.join(",", filenames));

                if (!StringUtils.isEmpty(oldProfilePicture)) {
                    cloudStorageService.deleteFile(bucketName, oldProfilePicture);
                }
            }

            userEntity.setCity(getOrDefault(profile.getCity(), userEntity.getCity()));
            userEntity.setGender(getOrDefault(profile.getGender(), userEntity.getGender()));
            userEntity.setDateOfBirth(getOrDefault(profile.getDateOfBirth(), userEntity.getDateOfBirth()));

            Optional<Client> optionalClient = clientRepository.findByUserID(profile.getUserID());
            if (optionalClient.isPresent()) {
                Client client = optionalClient.get();
                client.setRating(getOrDefault(profile.getRating(), client.getRating()));

                userRepository.save(userEntity);
                clientRepository.save(client);
            } else {
                throw new ResourceNotFoundException("Client profile not found");
            }
        } else {
            throw new ResourceNotFoundException("User not found");
        }
    }


    public String upsertFcmToken(FcmTokenRequest fcmTokenRequest) {

        try {

            if (fcmTokenRequest == null || fcmTokenRequest.getDeviceId() == null || fcmTokenRequest.getFcmToken() == null) {
                throw new IllegalArgumentException("Device ID or FCM Token is missing.");
            }

            logger.info("Processing FCM token for device: {}, user: {}", fcmTokenRequest.getDeviceId(), fcmTokenRequest.getUserId());

            // Check if a record with the given deviceId already exists
            FcmToken existingToken = fcmTokenRepository.findByUserIdAndDeviceId(fcmTokenRequest.getUserId(), fcmTokenRequest.getDeviceId());

            if (existingToken != null) {
                // Update the FCM token if the device already has an associated token
                existingToken.setFcmToken(fcmTokenRequest.getFcmToken());
                fcmTokenRepository.save(existingToken);

                logger.info("FCM token updated successfully for device: {}", fcmTokenRequest.getDeviceId());

                return "FCM token updated successfully";
            } else {
                // Insert a new record if the device does not have an associated token
                logger.info("No FCM token found for device: {}. Inserting new token.", fcmTokenRequest.getDeviceId());

                FcmToken newToken = new FcmToken();
                newToken.setUserId(fcmTokenRequest.getUserId());
                newToken.setDeviceId(fcmTokenRequest.getDeviceId());
                newToken.setFcmToken(fcmTokenRequest.getFcmToken());
                fcmTokenRepository.save(newToken);
                logger.info("FCM token inserted successfully for device: {}", fcmTokenRequest.getDeviceId());

                return "FCM token inserted successfully";
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new CreativePoolException(Errors.E00028.getMessage());
        }
    }


    public List<String> getFcmTokensByUserId(UUID userId,UserType userType) {
        try {
            logger.info("Fetching FCM tokens for userId: {}", userId);
            List<String> tokens=null;
            if(userType.equals(UserType.FREELANCER)){
                tokens=fcmTokenRepository.findFreelancerTokens(userId);
            }else{
                tokens=fcmTokenRepository.findClientTokens(userId);
            }
            if (tokens.isEmpty()) {
                logger.warn("No FCM tokens found for userId: {}", userId);
            }
            return tokens;
        } catch (Exception e) {
            logger.error("Error occurred while fetching FCM tokens for userId: {}", userId, e);
            throw new CreativePoolException("Unable to fetch FCM tokens. Please try again later.");
        }
    }


}


