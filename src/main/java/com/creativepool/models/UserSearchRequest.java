package com.creativepool.models;

import lombok.Data;

@Data
public class UserSearchRequest {

    Double rating;

    String priceRange;

    Integer page;

    Integer size;

}
