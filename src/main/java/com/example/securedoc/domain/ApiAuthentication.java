package com.example.securedoc.domain;

import com.example.securedoc.dto.User;
import com.example.securedoc.exception.ApiException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

public class ApiAuthentication extends AbstractAuthenticationToken {
    private static final String EMAIL_PROTECTED = "[EMAIL PROTECTED]";
    private static final String PASSWORD_PROTECTED = "[PASSWORD PROTECTED]";
    private User user;
    private final String email;
    private final String password;
    private final boolean authenticated;

    private ApiAuthentication(String email, String password) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.email = email;
        this.password = password;
        this.authenticated = false;
    }

    private ApiAuthentication(User user, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.user = user;
        this.email = EMAIL_PROTECTED;
        this.password = PASSWORD_PROTECTED;
        this.authenticated = true;
    }

    public static ApiAuthentication unauthenticated(String email, String password) {
        return new ApiAuthentication(email, password);
    }

    public static ApiAuthentication authenticated(User user, Collection<? extends GrantedAuthority> authorities) {
        return new ApiAuthentication(user, authorities);
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        throw new ApiException("Unauthorized to set authenticated");
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public Object getCredentials() {
        return PASSWORD_PROTECTED;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
