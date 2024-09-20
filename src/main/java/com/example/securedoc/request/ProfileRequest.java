package com.example.securedoc.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileRequest {
    @NotEmpty(message = "First name cannot be empty or null")
    private String firstName;
    @NotEmpty(message = "Last name cannot be empty or null")
    private String lastName;
    @NotEmpty(message = "Email cannot be empty or null")
    @Email(message = "Invalid email address")
    private String email;
    private String phone;
    private String bio;
    @NotEmpty(message = "Image URL cannot be empty or null")
    private String imageUrl;
    private boolean mfa;
}
