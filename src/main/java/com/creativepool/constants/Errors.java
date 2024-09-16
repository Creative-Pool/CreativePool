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
    E00013("Something went wrong"),
    E00014("User doesn't exist"),
    E00015("Error in fetching profile data"),
    E00016("Failed to submit freelancer feedback"),
    E00017("Failed to submit client feedback"),
    E00018("Unable to create ticket"),
    E00019("Failed to assign ticket due to an unexpected error."),
    E00020("An error occurred while editing the ticket."),
    E00021("An error occurred while fetching tickets."),
    E00022("An error occurred while fetching applicants."),
    E00023("An error occurred while fetching freelancers."),
    E00024("Unexpected error occurred while rejecting freelancer request."),
    E00025("Unexpected error occurred while rejecting client request."),
    E00026("Error in creating meeting");

    private final String message;
    Errors(String message) {
        this.message = message;
    }

}
