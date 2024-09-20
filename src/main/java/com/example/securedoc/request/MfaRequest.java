package com.example.securedoc.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MfaRequest {
    @NotEmpty(message = "Code cannot be empty or null")
    @Size(min = 6, max = 6, message = "Code has to be 6 digits")
    private String code;
}
