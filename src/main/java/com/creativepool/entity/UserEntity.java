package com.creativepool.entity;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Date;
import java.util.UUID;
@Data
@Entity(name = "account")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private UUID userID;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "isactive")
    private Boolean isActive;

    @Column(name = "isdeleted")
    private Boolean isDeleted;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "updated_date")
    private Date updatedDate;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "city")
    private String city;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="user_type")
    private UserType userType;

}
