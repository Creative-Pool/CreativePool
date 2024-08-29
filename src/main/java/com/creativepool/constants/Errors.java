package com.creativepool.constants;

import lombok.Getter;

@Getter
public enum Errors {
    E00001("Required Inputs is/are invalid"),
    E00002("Please complete your profile"),
    E00003("User Already exists."),
    E00004("Ticket not found with id: %s"),
    E00005("A freelancer profile with the same user ID already exists."),
    E00006("Email already exists."),
    E00007("Contact no already exists."),
    E00008("You have reached the maximum number of assigned tickets."),
    E00009("Unable to upload the file."),
    E00010("Request Id is not present."),
    E00011("Unable to create a record for the client reach-out. Please try again later"),
    E00012("Unable to create a record for the freelancer reach-out. Please try again later"),
    E00013("Something went wrong");

    private final String message;
    Errors(String message) {
        this.message = message;
    }

}
