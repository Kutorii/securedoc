package com.example.securedoc.service.impl;

import com.example.securedoc.cache.CacheStore;
import com.example.securedoc.domain.RequestContext;
import com.example.securedoc.dto.User;
import com.example.securedoc.entity.ConfirmationEntity;
import com.example.securedoc.entity.CredentialEntity;
import com.example.securedoc.entity.RoleEntity;
import com.example.securedoc.entity.UserEntity;
import com.example.securedoc.enumeration.LoginType;
import com.example.securedoc.event.UserEvent;
import com.example.securedoc.exception.RegisterException;
import com.example.securedoc.exception.entity.*;
import com.example.securedoc.repository.ConfirmationRepository;
import com.example.securedoc.repository.CredentialRepository;
import com.example.securedoc.repository.RoleRepository;
import com.example.securedoc.repository.UserRepository;
import com.example.securedoc.service.UserService;
import com.example.securedoc.utils.MfaUtils;
import com.example.securedoc.utils.UserUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.example.securedoc.constant.Constants.MAXIMUM_LOGIN_ATTEMPTS;
import static com.example.securedoc.enumeration.Authority.*;
import static com.example.securedoc.enumeration.EventType.*;
import static java.time.LocalDateTime.*;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final ConfirmationRepository confirmationRepository;
    private final CredentialRepository credentialRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;
    private final CacheStore<String, Integer> userLoginCache;
    private final BCryptPasswordEncoder encoder;

    @Override
    public void createUser(String firstName, String lastName, String email, String password) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new RegisterException("Registration failed. Please try again");
        }

        RequestContext.setUserId(0L);
        var userEntity = userRepository.save(createNewUser(firstName, lastName, email));
        var credentialEntity = new CredentialEntity(encoder.encode(password), userEntity);

        RequestContext.setUserId(0L);
        credentialRepository.save(credentialEntity);

        var confirmationEntity = new ConfirmationEntity(userEntity);
        RequestContext.setUserId(0L);
        confirmationRepository.save(confirmationEntity);

        publisher.publishEvent(new UserEvent(userEntity, REGISTRATION, Map.of("key", confirmationEntity.getKey())));
    }

    @Override
    public RoleEntity getRoleName(String name) {
        var role = roleRepository.findByNameIgnoreCase(name);

        return role.orElseThrow(() -> new RoleEntityNotFoundException("Role not found"));
    }

    @Override
    public User getUserByUserId(String userId) {
        var userEntity = userRepository.findByUserId(userId).orElseThrow(() -> new UserEntityNotFoundException("User not found"));

        return UserUtils.fromEntity(userEntity, userEntity.getRole(), getUserCredentialsById(userEntity.getId()));
    }

    @Override
    public User getUserByEmail(String email) {
        var userEntity = getUserEntityByEmail(email);

        return UserUtils.fromEntity(userEntity, userEntity.getRole(), getUserCredentialsById(userEntity.getId()));
    }

    @Override
    public CredentialEntity getUserCredentialsById(Long userId) {
        var credentialById = credentialRepository.getCredentialByUserId(userId);

        return credentialById.orElseThrow(() -> new CredentialEntityNotFoundException("Credentials not found"));
    }

    private UserEntity createNewUser(String firstName, String lastName, String email) {
        var role = getRoleName(USER.name());

        return UserUtils.createUserEntity(firstName, lastName, email, role);
    }

    @Override
    public void verifyAccountKey(String key) {
        var confirmationEntity = getConfirmationEntity(key);
        checkConfirmationExpiration(confirmationEntity);

        var userEntity = getUserEntityByEmail(confirmationEntity.getUser().getEmail());
        userEntity.setEnabled(true);
        RequestContext.setUserId(userEntity.getId());
        userRepository.save(userEntity);
        confirmationRepository.delete(confirmationEntity);
    }

    private void checkConfirmationExpiration(ConfirmationEntity confirmationEntity) {
        if (confirmationEntity.isExpired()) {
            confirmationRepository.delete(confirmationEntity);
            throw new ConfirmationEntityNotFoundException("Confirmation expired");
        }
    }

    private ConfirmationEntity getConfirmationEntity(String key) {
        var confirmationEntity = confirmationRepository.findByKey(key);

        return confirmationEntity.orElseThrow(() -> new ConfirmationEntityNotFoundException("Confirmation not found"));
    }

    private UserEntity getUserEntityByEmail(String email) {
        var userEntity = userRepository.findByEmailIgnoreCase(email);

        return userEntity.orElseThrow(() -> new UserEntityNotFoundException("User not found"));
    }

    @Override
    public void updateLoginAttempt(String email, LoginType loginType) {

        var userEntity = getUserEntityByEmail(email);
        RequestContext.setUserId(userEntity.getId());

        switch (loginType) {
            case LOGIN_ATTEMPT -> {
                if (userLoginCache.get(userEntity.getEmail()) == null) {
                    userEntity.setLoginAttempts(0);
                    userEntity.setAccountNonLocked(true);
                }

                userEntity.setLoginAttempts(userEntity.getLoginAttempts() + 1);
                userLoginCache.put(userEntity.getEmail(), userEntity.getLoginAttempts());

                if (userLoginCache.get(userEntity.getEmail()) > MAXIMUM_LOGIN_ATTEMPTS) {
                    userEntity.setAccountNonLocked(false);
                }
            }

            case LOGIN_SUCCESSFUL -> {
                userEntity.setAccountNonLocked(true);
                userEntity.setLoginAttempts(0);
                userEntity.setLastLogin(now());
                userLoginCache.invalidate(userEntity.getEmail());
            }
        }

        userRepository.save(userEntity);
    }

    @Override
    public User updateUser(User user) {
        var userEntity = getUserEntityByUserId(user.getUserId());
        RequestContext.setUserId(userEntity.getId());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setBio(user.getBio());
        userEntity.setPhone(user.getPhone());
        userEntity.setImageUrl(user.getImageUrl());

        // User enabled MFA
        if (user.isMfa() && !userEntity.isMfa()) {
            var secret = MfaUtils.buildSecret.get();
            userEntity.setQrCodeSecret(secret);
            var qrUri = MfaUtils.getQrCodeUri(secret, userEntity.getEmail());
            userEntity.setQrCodeUri(qrUri);
        }

        // User disabled MFA
        if (!user.isMfa() && userEntity.isMfa()) {
            userEntity.setQrCodeUri(null);
            userEntity.setQrCodeSecret(null);
        }

        userEntity = userRepository.save(userEntity);

        return UserUtils.fromEntity(userEntity, userEntity.getRole(), getUserCredentialsById(userEntity.getId()));
    }

    private UserEntity getUserEntityByUserId(String userId) {
        var userEntity = userRepository.findByUserId(userId);

        return userEntity.orElseThrow(() -> new UserEntityNotFoundException("User not found"));
    }

    @Override
    public String getUserMfaSecretByEmail(String email) {
        var userEntity = getUserEntityByEmail(email);

        return userEntity.getQrCodeSecret();
    }

    @Override
    public void forgotPasswordRequest(String email) {
        var userEntityOptional = userRepository.findByEmailIgnoreCase(email);

        if (userEntityOptional.isPresent()) {
            var userEntity = userEntityOptional.get();
            var confirmationEntity = new ConfirmationEntity(userEntity);
            RequestContext.setUserId(userEntity.getId());
            confirmationRepository.save(confirmationEntity);
            publisher.publishEvent(new UserEvent(userEntity, PASSWORD_RESET, Map.of("key", confirmationEntity.getKey())));
        }
    }

    @Override
    public void verifyResetPasswordToken(String token) {
        var confirmationEntity = getConfirmationEntity(token);
        checkConfirmationExpiration(confirmationEntity);
    }

    @Override
    public void resetPassword(String token, String password) {
        var confirmationEntity = getConfirmationEntity(token);
        checkConfirmationExpiration(confirmationEntity);
        var userEntityId = confirmationEntity.getUser().getId();
        var credentialEntity = getUserCredentialsById(userEntityId);
        RequestContext.setUserId(userEntityId);
        confirmationRepository.delete(confirmationEntity);
        credentialEntity.setPassword(encoder.encode(password));
        credentialRepository.save(credentialEntity);
    }
}
