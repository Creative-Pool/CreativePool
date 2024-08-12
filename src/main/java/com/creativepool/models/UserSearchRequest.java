package com.creativepool.models;

import lombok.Data;

@Data
public class UserSearchRequest {

    Double rating;
    String priceRange;
    String username;
    String firstname;
    String lastname;
    Integer page;
    Integer size;

}
