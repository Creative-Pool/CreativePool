package com.creativepool.models;

import com.creativepool.entity.EducationalQualificationType;
import com.creativepool.entity.Gender;
import com.creativepool.entity.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Profile {
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String profileImage;
    private Date dateOfBirth;
    private Gender gender;
    private String city;
    private UUID userID;
    private Double rating;
    private String bio;
    private UUID clientId;
    private UUID freelancerId;
    private UserType userType;
    private EducationalQualificationType educationalQualification;
    private BigDecimal minCharges;
    private List<WorkHistory> workHistory;

}
