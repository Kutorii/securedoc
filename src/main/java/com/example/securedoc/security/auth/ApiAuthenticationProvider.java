package com.example.securedoc.security.auth;

import com.example.securedoc.domain.ApiAuthentication;
import com.example.securedoc.domain.UserPrincipal;
import com.example.securedoc.exception.ApiException;
import com.example.securedoc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

import static java.time.LocalDateTime.now;

@Component
@RequiredArgsConstructor
public class ApiAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;
    private final BCryptPasswordEncoder encoder;

    private final Function<Authentication, ApiAuthentication> getApiAuthentication = authentication -> (ApiAuthentication) authentication;

    private final Consumer<UserPrincipal> validAccount = userPrincipal -> {
        if (!userPrincipal.isAccountNonExpired()) {
            throw new DisabledException("Account is expired");
        }
        if (!userPrincipal.isAccountNonLocked()) {
            throw new LockedException("Account is locked");
        }
        if (!userPrincipal.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("Credentials is expired");
        }
        if (!userPrincipal.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }
    };

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var apiAuth = getApiAuthentication.apply(authentication);
        var user = userService.getUserByEmail(apiAuth.getEmail());

        if (user != null) {
            var userCredentials = userService.getUserCredentialsById(user.getId());
//            if (userCredentials.getUpdatedAt().plusDays(NINETY_DAYS).isBefore(now())) {
//                throw new ApiException("Credentials are expired");
//            }

            var userPrincipal = new UserPrincipal(user, userCredentials);
            validAccount.accept(userPrincipal);

            if (encoder.matches(apiAuth.getPassword(), userCredentials.getPassword())) {
                return ApiAuthentication.authenticated(user, userPrincipal.getAuthorities());
            }
            throw new ApiException("Invalid credentials");
        } else {
            throw new ApiException("Unable to authenticate");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ApiAuthentication.class.isAssignableFrom(authentication);
    }
}
