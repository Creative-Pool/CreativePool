package com.creativepool.service;

import com.creativepool.constants.Errors;
import com.creativepool.entity.*;
import com.creativepool.exception.BadRequestException;
import com.creativepool.models.Profile;
import com.creativepool.models.User;
import com.creativepool.repository.ClientRepository;
import com.creativepool.repository.FreelancerRepository;
import com.creativepool.repository.UserRepository;
import org.apache.commons.lang3.ObjectUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FreelancerRepository freelancerRepository;

    @Autowired
    ClientRepository clientRepository;

    public void createUser(User user) {
        try {
            if (ObjectUtils.isEmpty(user))
                throw new BadRequestException(Errors.E00001.getMessage());
            UserEntity userEntity = userRepository.findByUsername(user.getUsername());
            if (!ObjectUtils.isEmpty(userEntity))
                throw new BadRequestException(Errors.E00003.getMessage());

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
            userRepository.save(userEntity);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("account_email_key"))
                throw new DataIntegrityViolationException(Errors.E00006.getMessage());
            if (e.getMessage().contains("unique_phone_no"))
                throw new DataIntegrityViolationException(Errors.E00007.getMessage());
        }
    }

    public void createProfile(Profile profile) {
        try {
            switch (profile.getUserType().toString()) {
                case "FREELANCER":
                    createFreelancerProfile(profile);
                    break;
                case "CLIENT":
                    createClientProfile(profile);
                    break;
            }
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(Errors.E00005.getMessage());
        }
    }

    private void createFreelancerProfile(Profile profile) {

            Optional<UserEntity> optionalUserEntity = userRepository.findById(profile.getUserID());
            UserEntity userEntity;
            if (optionalUserEntity.isPresent()) {
                userEntity = optionalUserEntity.get();

                userEntity.setCity(profile.getCity());
                userEntity.setGender(profile.getGender());
                userEntity.setDateOfBirth(profile.getDateOfBirth());
                userEntity.setProfileImage(profile.getProfileImage());


                Freelancer freelancer = new Freelancer();
                freelancer.setId(UUID.randomUUID());
                freelancer.setBio(profile.getBio());
                freelancer.setRating(profile.getRating());
                freelancer.setEducationalQualification(profile.getEducationalQualification());
                freelancer.setUserID(profile.getUserID());
                freelancer.setTotalAssignedTickets(0);

                userRepository.save(userEntity);
                freelancerRepository.save(freelancer);
            }

    }

    private void createClientProfile(Profile profile){

        Optional<UserEntity> optionalUserEntity = userRepository.findById(profile.getUserID());
        UserEntity userEntity;
        if (optionalUserEntity.isPresent()) {
            userEntity = optionalUserEntity.get();

            userEntity.setCity(profile.getCity());
            userEntity.setGender(profile.getGender());
            userEntity.setDateOfBirth(profile.getDateOfBirth());
            userEntity.setProfileImage(profile.getProfileImage());



            Client client = new Client();
            client.setClientID(UUID.randomUUID());
            client.setRating(profile.getRating());
            client.setUserID(profile.getUserID());

            userRepository.save(userEntity);
            clientRepository.save(client);
        }
    }

    public Profile getProfile(UUID id, UserType userType) {

        return switch (userType.toString()) {
            case "FREELANCER" -> getFreelancerProfile(id);
            case "CLIENT" -> getClientProfile(id);
            default -> new Profile();
        };
    }


    private Profile getFreelancerProfile(UUID id){
        Profile profile=new Profile();
        Optional<Freelancer> optionalFreelancerID = freelancerRepository.findById(id);

        if(optionalFreelancerID.isPresent()) {
            Freelancer freelancer = optionalFreelancerID.get();
            UserEntity userEntity = userRepository.findById(freelancer.getUserID()).get();
            mapUserEntityToProfile(profile,userEntity);
            profile.setBio(freelancer.getBio());
            profile.setRating(freelancer.getRating());
            profile.setEducationalQualification(freelancer.getEducationalQualification());

        }
        return profile;
    }

    private Profile getClientProfile(UUID id) {
        Profile profile = new Profile();
        Optional<Client> optionalClientID = clientRepository.findById(id);

        if (optionalClientID.isPresent()) {
            Client client = optionalClientID.get();
            UserEntity userEntity = userRepository.findById(client.getUserID()).get();
            profile.setRating(client.getRating());
            mapUserEntityToProfile(profile, userEntity);
        } else {
            throw new BadRequestException(Errors.E00002.getMessage());
        }
        return profile;
    }

    private void mapUserEntityToProfile(Profile profile,UserEntity userEntity){
        profile.setFirstName(userEntity.getFirstName());
        profile.setLastName(userEntity.getLastName());
        profile.setCity(userEntity.getCity());
        profile.setEmail(userEntity.getEmail());
        profile.setDateOfBirth(userEntity.getDateOfBirth());
        profile.setGender(userEntity.getGender());
        profile.setPhone(userEntity.getPhone());
        profile.setUsername(userEntity.getUsername());
        profile.setUserID(userEntity.getUserID());
        profile.setProfileImage(userEntity.getProfileImage());
        profile.setUserType(userEntity.getUserType());
        profile.setPhone(userEntity.getPhone());
    }

}


