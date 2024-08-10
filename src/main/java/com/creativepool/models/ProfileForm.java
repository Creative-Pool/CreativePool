package com.creativepool.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileForm {
    private String profile;

    private MultipartFile file;

    public Profile getProfile()  {
        ObjectMapper ob = new ObjectMapper();
        try {

            return ob.readValue(profile, Profile.class);
        } catch (Exception ex) {
            String message = ex.getMessage();
        }
        return null;
    }


}
