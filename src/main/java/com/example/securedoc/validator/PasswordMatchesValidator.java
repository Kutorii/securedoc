package com.example.securedoc.validator;

import com.example.securedoc.request.ForgotPasswordRequest;
import com.example.securedoc.request.ResetPasswordRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, ResetPasswordRequest> {

    @Override
    public boolean isValid(ResetPasswordRequest forgotPasswordRequest, ConstraintValidatorContext constraintValidatorContext) {
        if (forgotPasswordRequest == null) {
            return true;
        }

        return forgotPasswordRequest.getPassword().equals(forgotPasswordRequest.getConfirmPassword());
    }
}
