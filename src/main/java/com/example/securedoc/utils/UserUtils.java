package com.example.securedoc.utils;

import com.example.securedoc.dto.User;
import com.example.securedoc.entity.CredentialEntity;
import com.example.securedoc.entity.RoleEntity;
import com.example.securedoc.entity.UserEntity;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

import static com.example.securedoc.constant.Constants.NINETY_DAYS;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class UserUtils {
    public static UserEntity createUserEntity(String firstName, String lastName, String email, RoleEntity role) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .role(role)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .mfa(false)
                .lastLogin(now())
                .enabled(false)
                .loginAttempts(0)
                .qrCodeSecret(EMPTY)
                .phone(EMPTY)
                .bio(EMPTY)
                .imageUrl("https://cdn-icons-png.flaticon.com/512/3736/3736502.png")
                .build();
    }

    public static User fromEntity(UserEntity userEntity, RoleEntity role, CredentialEntity credentialEntity) {
        var user = new User();
        BeanUtils.copyProperties(userEntity, user);
        user.setLastLogin(userEntity.getLastLogin().toString());
        user.setCredentialsNonExpired(isCredentialsNonExpired(credentialEntity));
        user.setCreatedAt(user.getCreatedAt());
        user.setUpdatedAt(user.getUpdatedAt());
        user.setRole(role.getName());
        user.setAuthorities(role.getAuthorities().getValue());

        return user;
    }

    private static boolean isCredentialsNonExpired(CredentialEntity credentialEntity) {
        return credentialEntity.getUpdatedAt().plusDays(NINETY_DAYS).isAfter(now());
    }
}
