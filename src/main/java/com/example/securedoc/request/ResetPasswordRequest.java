package com.example.securedoc.request;

import com.example.securedoc.validator.PasswordMatches;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@PasswordMatches
public class ResetPasswordRequest {
    @NotEmpty(message = "Password cannot be empty or null")
    private String password;

    @NotEmpty(message = "Password cannot be empty or null")
    private String confirmPassword;
}
