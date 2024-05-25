package com.creativepool.models;

import com.creativepool.entity.Gender;
import com.creativepool.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String profileImage;
    private Date createdDate;
    private Date updatedDate;
    private Date dateOfBirth;
    private Gender gender;
    private String city;
    private UserType userType;
}
