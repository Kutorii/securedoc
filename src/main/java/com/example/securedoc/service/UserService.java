package com.example.securedoc.service;

import com.example.securedoc.dto.User;
import com.example.securedoc.entity.CredentialEntity;
import com.example.securedoc.entity.RoleEntity;
import com.example.securedoc.enumeration.LoginType;
import jakarta.servlet.http.HttpSession;

public interface UserService {

    void createUser(String firstName, String lastName, String email, String password);

    RoleEntity getRoleName(String name);

    User getUserByUserId(String userId);

    void verifyAccountKey(String key);

    void updateLoginAttempt(String email, LoginType loginType);

    User getUserByEmail(String email);

    CredentialEntity getUserCredentialsById(Long id);

    User updateUser(User user);

    String getUserMfaSecretByEmail(String email);

    void forgotPasswordRequest(String email);

    void verifyResetPasswordToken(String token);

    void resetPassword(String token, String password);
}
